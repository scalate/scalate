package org.fusesource.scalate.jersey

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
 * @version $Revision : 1.1 $
 */
class SSPTemplateProcessor(@Context resourceConfig: ResourceConfig) extends ViewProcessor[String] with Logging {
  
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

  var errorUris: List[String] = List("/WEB-INF/errors/500.scaml", "/WEB-INF/errors/500.ssp")

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
