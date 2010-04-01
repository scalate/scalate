package org.fusesource.scalate.jersey

import _root_.org.fusesource.scalate.servlet.TemplateEngineServlet
import java.io.OutputStream
import java.net.MalformedURLException
import javax.servlet.ServletContext
import com.sun.jersey.api.view.Viewable
import com.sun.jersey.spi.template.ViewProcessor
import com.sun.jersey.server.impl.container.servlet.RequestDispatcherWrapper
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.sun.jersey.api.core.{HttpContext, ResourceConfig}
import org.fusesource.scalate.util.Logging
import com.sun.jersey.api.container.ContainerException
import javax.ws.rs.core.Context

/**
 * A template processor for <a href="https://jersey.dev.java.net/">Jersey</a> using Scalate templates
 * @version $Revision : 1.1 $
 */
class ScalateTemplateProcessor(@Context resourceConfig: ResourceConfig) extends ViewProcessor[String] with Logging {
  
  @Context
  var servletContext: ServletContext = _
  @Context
  var hc: HttpContext = _
  @Context
  var request: HttpServletRequest = _
  @Context
  var response: HttpServletResponse = _

  val basePath = resourceConfig.getProperties().get("org.fusesource.config.property.SSPTemplatesBasePath") match {
    case path: String => if (path(0) == '/') path else "/" + path
    case _            => ""
  }

  // TODO it would be nice to be able to slurp these out of the web.xml or Servlet 3 configuration
  // so that they reused whatever the web app was setup to use...
  var errorUris: List[String] = List("/WEB-INF/scalate/errors/500.scaml", "/WEB-INF/scalate/errors/500.ssp")

  var templateSuffixes = List("", ".ssp", ".scaml")
  var templateDirectories = List("/WEB-INF", "")

  def resolve(requestPath: String): String = {
    if (servletContext == null) {
      warn("No servlet context")
      return null
    }

    try {
      val path = if (basePath.length > 0) basePath + requestPath else requestPath
      
      tryFindPath(path) match {
        case Some(answer) => answer
        case None => 
          /* 
            before Jersey 1.2 paths were often searched as 
              com/acme/foo/SomeClass/index.ssp
            however we prefer to use this naming convention
              com/acme/foo/SomeClass.index.ssp
            so lets add a little hook in here
           */
          val idx = path.lastIndexOf('/')
          if (idx > 1) {
            val newPath = path.substring(0, idx) + "." + path.substring(idx + 1)
            tryFindPath(newPath).getOrElse(null) 
          }
          else {
            null
          }
      }
    } catch {
      case e: MalformedURLException =>
        warn("Tried to load template using Malformed URL. " + e.getMessage)
        null
    }
  }

  def tryFindPath(path: String): Option[String] = {
    val paths = for (prefix <- templateDirectories; postfix <- templateSuffixes) yield prefix + path + postfix

    paths.find {
      t =>
        debug("Trying to find template: " + t)
        servletContext.getResource(t) ne null
    }
  }

  def writeTo(resolvedPath: String, viewable: Viewable, out: OutputStream): Unit = {
    // Ensure headers are committed
    out.flush()

    val servlet = TemplateEngineServlet()
    if (servlet == null) {
      // we have not been initialised yet so lets use the request dispatcher
      writeToUsingRequestDispatcher(resolvedPath, viewable, out)
    }
    else {
      writeToUsingServlet(servlet, resolvedPath, viewable, out)
    }
  }

  /**
   * Renders the template using the servlet instance directly as its a little more efficient
   * plus it avoids issues with using jersey with guice-servlet not correctly dispatching to the servlet
   */
  def writeToUsingServlet(servlet: TemplateEngineServlet, resolvedPath: String, viewable: Viewable, out: OutputStream): Unit = {
    // lets use the singleton servlet as its a bit more efficient and avoids issues with
    // using jersey with guice-servlet not correctly dispatching to the servlet
    request.setAttribute("it", viewable.getModel)

    try {
      servlet.render(resolvedPath, request, response)
    } catch {
      case e: Exception =>
        // lets forward to the error handler
        var notFound = true
        for (uri <- errorUris if notFound) {
          if (servletContext.getResource(uri) != null) {

            // we need to expose all the errors property here...
            request.setAttribute("javax.servlet.error.exception", e)
            request.setAttribute("javax.servlet.error.exception_type", e.getClass)
            request.setAttribute("javax.servlet.error.message", e.getMessage)
            request.setAttribute("javax.servlet.error.request_uri", request.getRequestURI)
            request.setAttribute("javax.servlet.error.servlet_name", request.getServerName)

            // TODO how to get the status code???
            val status = 500
            request.setAttribute("javax.servlet.error.status_code", status)

            request.setAttribute("it", e)
            servlet.render(uri, request, response)
            notFound = false
          }
        }
        if (notFound) {
          throw new ContainerException(e)
        }

        // throw new ContainerException(e)
    }
  }

  def writeToUsingRequestDispatcher(resolvedPath: String, viewable: Viewable, out: OutputStream): Unit = {
    val dispatcher = servletContext.getRequestDispatcher(resolvedPath)
    if (dispatcher == null) {
      throw new ContainerException("No request dispatcher for: " + resolvedPath)
    }

    try {
      val wrapper = new RequestDispatcherWrapper(dispatcher, basePath, hc, viewable)
      wrapper.forward(request, response)
      //wrapper.forward(requestInvoker.get(), responseInvoker.get())
    } catch {
      case e: Exception =>
        // lets forward to the error handler
        var notFound = true
        for (uri <- errorUris if notFound) {
          val rd = servletContext.getRequestDispatcher(uri)
          if (rd != null) {

            // we need to expose all the errors property here...
            request.setAttribute("javax.servlet.error.exception", e)
            request.setAttribute("javax.servlet.error.exception_type", e.getClass)
            request.setAttribute("javax.servlet.error.message", e.getMessage)
            request.setAttribute("javax.servlet.error.request_uri", request.getRequestURI)
            request.setAttribute("javax.servlet.error.servlet_name", request.getServerName)

            // TODO how to get the status code???
            val status = 500
            request.setAttribute("javax.servlet.error.status_code", status)

            val rdw = new RequestDispatcherWrapper(rd, basePath, hc, new Viewable(uri, e))
            rdw.forward(request, response)
            notFound = false
          }
        }
        if (notFound) {
          throw new ContainerException(e)
        }

        // throw new ContainerException(e)
    }
  }
}
