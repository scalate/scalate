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
import java.lang.{String, Class}
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import org.fusesource.scalate.support.ResourceLoader
import javax.ws.rs.ext.{Provider, MessageBodyWriter, MessageBodyReader}
import javax.ws.rs.Produces
import org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine, ServletHelper, TemplateEngineServlet}
import javax.ws.rs.core.{UriInfo, MultivaluedMap, MediaType, Context}
import org.fusesource.scalate.{Binding, TemplateEngine}

/**
 * A template provider for <a href="https://jersey.dev.java.net/">Jersey</a> using Scalate templates
 * to produce HTML of an object.
 *
 * @version $Revision : 1.1 $
 */
@Provider
class ScalateTemplateProvider extends MessageBodyWriter[AnyRef] with Logging {

  @Context
  var servletContext: ServletContext = _
  @Context
  var request: HttpServletRequest = _
  @Context
  var response: HttpServletResponse = _
  @Context
  var uriInfo:UriInfo = _

  var templateDirectories = List("/WEB-INF", "")

  def resolve(loader:ResourceLoader, argType:Class[_]):String = {
    val argBase = argType.getName.replace('.','/')

    templateDirectories.foreach { dir =>
      TemplateEngine.templateTypes.foreach { types =>
        val path = dir + "/" + argBase + "." + types
        if( loader.exists(path) ) {
          return path
        }
      }
    }
    null
  }

  def getSize(arg: AnyRef, argType : Class[_], genericType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L

  def isWriteable(argType: Class[_], genericType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    if(mediaType.getType == "text" && mediaType.getSubtype == "html") {
      val servlet = TemplateEngineServlet()
      val path = resolve(servlet.templateEngine.resourceLoader, argType)
      path != null
    } else {
      false
    }
  }

  def writeTo(arg:AnyRef, argType:Class[_], genericType: Type, annotations: Array[Annotation], media: MediaType, headers: MultivaluedMap[String, AnyRef], out: OutputStream) = {
    // Ensure headers are committed
    out.flush()

    val servlet = TemplateEngineServlet()
    val path = resolve(servlet.templateEngine.resourceLoader, argType)

    try {

      assert(path != null)

      request.setAttribute("uri_info", uriInfo)
      request.setAttribute("it", arg)

      val context = new ServletRenderContext(servlet.templateEngine, request, response, servletContext)
      context.include(path, true, List(Binding("it", argType.getName, false, None, "val",  true )))


    } catch {
      case e: Exception =>
        // lets forward to the error handler
        var notFound = true
        for (uri <- ServletHelper.errorUris() if notFound) {
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
    }
  }




}