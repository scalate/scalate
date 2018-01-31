/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.rest

import java.lang.annotation.Annotation
import java.lang.reflect.Type
import java.io.OutputStream
import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }
import javax.ws.rs.ext.{ Provider, MessageBodyWriter }
import javax.ws.rs.core.{ UriInfo, MultivaluedMap, MediaType, Context }
import org.fusesource.scalate.servlet.{ ServletRenderContext, ServletTemplateEngine, ServletHelper, TemplateEngineServlet }
import org.fusesource.scalate.TemplateException
import org.fusesource.scalate.util.{ Log, ResourceNotFoundException }

import javax.ws.rs.WebApplicationException

object ScalateTemplateProvider extends Log;

/**
 * A template provider for <a href="https://jersey.dev.java.net/">Jersey</a> using Scalate templates
 * to produce HTML of an object.
 *
 * @version $Revision : 1.1 $
 */
@Provider
class ScalateTemplateProvider extends MessageBodyWriter[AnyRef] {

  @Context
  var servletContext: ServletContext = _
  @Context
  var request: HttpServletRequest = _
  @Context
  var response: HttpServletResponse = _
  @Context
  var uriInfo: UriInfo = _

  def resolve(engine: ServletTemplateEngine, argType: Class[_]): String = {
    val argBase = argType.getName.replace('.', '/')
    engine.extensions.foreach { ext =>
      val path = "/" + argBase + "." + ext
      try {
        engine.load(path)
        return path
      } catch {
        case x: ResourceNotFoundException =>
        case x: TemplateException =>
          return path
      }
    }
    null
  }

  def getSize(arg: AnyRef, argType: Class[_], genericType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L

  def isWriteable(argType: Class[_], genericType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    var answer = false
    if (mediaType.getType == "text" && mediaType.getSubtype == "html") {
      val engine = ServletTemplateEngine(servletContext)
      if (engine != null && engine.resourceLoader != null) {
        val path = resolve(engine, argType)
        answer = path != null
      }
    }
    answer
  }

  def writeTo(arg: AnyRef, argType: Class[_], genericType: Type, annotations: Array[Annotation], media: MediaType, headers: MultivaluedMap[String, AnyRef], out: OutputStream) = {
    // Ensure headers are committed
    out.flush()

    val engine = ServletTemplateEngine(servletContext)
    val path = resolve(engine, argType)

    try {

      assert(path != null)

      request.setAttribute("uri_info", uriInfo)
      request.setAttribute("it", arg)

      val context = new ServletRenderContext(engine, request, response, servletContext)
      context.include(path, true)

    } catch {
      case e: Exception =>
        // lets forward to the error handler
        var notFound = true
        for (uri <- ServletHelper.errorUris() if notFound) {
          try {
            request.setAttribute("javax.servlet.error.exception", e)
            request.setAttribute("javax.servlet.error.exception_type", e.getClass)
            request.setAttribute("javax.servlet.error.message", e.getMessage)
            request.setAttribute("javax.servlet.error.request_uri", request.getRequestURI)
            request.setAttribute("javax.servlet.error.servlet_name", request.getServerName)

            // TODO how to get the status code???
            val status = 500
            request.setAttribute("javax.servlet.error.status_code", status)

            request.setAttribute("it", e)
            TemplateEngineServlet.render(uri, engine, servletContext, request, response)
            notFound = false
          } catch {
            case _: Exception =>
          }
        }
        if (notFound) {
          throw createContainerException(e)
        }
    }
  }

  protected def createContainerException(e: Exception) = {
    new WebApplicationException(e)
  }

}
