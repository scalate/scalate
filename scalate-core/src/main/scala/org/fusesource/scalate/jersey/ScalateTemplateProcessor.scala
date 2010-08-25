/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.servlet.{ServletTemplateEngine, ServletHelper, TemplateEngineServlet}

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
    case _ => ""
  }

  var templateTypes: List[String] = TemplateEngine.templateTypes
  var errorUris: List[String] = ServletHelper.errorUris()
  var templateSuffixes: List[String] = templateTypes.map(s => "." + s) :+ ""
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
    def findPath(filter: String => Boolean): Option[String] = {
      for (prefix <- templateDirectories; postfix <- templateSuffixes) {
        val p = prefix + path + postfix
        if (filter(p)) return Some(p)
      }
      None
    }

    val servlet = TemplateEngineServlet()
    if (servlet != null) {
      val loader = servlet.templateEngine.resourceLoader
      findPath(loader.resource(_).isDefined)
    } else {
      findPath(servletContext.getResource(_) ne null)
    }
  }


  def writeTo(resolvedPath: String, viewable: Viewable, out: OutputStream): Unit = {
    // Ensure headers are committed
    out.flush()

    val engine = if (servletContext != null) ServletTemplateEngine(servletContext) else null
    if (engine == null) {
      // we have not been initialised yet so lets use the request dispatcher
      writeToUsingRequestDispatcher(resolvedPath, viewable, out)
    }
    else {
      writeToUsingServletTemplateEngine(engine, resolvedPath, viewable, out)
    }
  }

  /**
   * Renders the template using the template engine registered with the servlet context directly as its a
   * little more efficient plus it avoids issues with using jersey with
   * guice-servlet not correctly dispatching to the servlet
   */
  def writeToUsingServletTemplateEngine(engine: TemplateEngine, resolvedPath: String, viewable: Viewable, out: OutputStream): Unit = {
    // lets use the singleton servlet as its a bit more efficient and avoids issues with
    // using jersey with guice-servlet not correctly dispatching to the servlet
    request.setAttribute("it", viewable.getModel)

    def render(template: String) = TemplateEngineServlet.render(template, engine, servletContext, request, response)

    try {
      render(resolvedPath)
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
            render(uri)
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
