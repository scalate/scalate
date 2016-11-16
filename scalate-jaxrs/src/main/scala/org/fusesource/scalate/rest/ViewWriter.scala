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

import java.io.OutputStream
import java.lang.reflect.Type
import javax.ws.rs.ext.{MessageBodyWriter, Provider}
import javax.servlet.ServletContext
import javax.ws.rs.core.{Context, MultivaluedMap, MediaType}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.lang.{String, Class}
import java.lang.annotation.Annotation
import org.fusesource.scalate.support.TemplateFinder
import org.fusesource.scalate.servlet.{ServletTemplateEngine, ServletHelper, TemplateEngineServlet}
import org.fusesource.scalate.util.{Log, ResourceNotFoundException, Logging}
import javax.ws.rs.core.UriInfo
import javax.ws.rs.WebApplicationException

object ViewWriter extends Log

/**
 * Renders a [[org.fusesource.scalate.rest.View]] using the Scalate template engine 
 *
 * @version $Revision : 1.1 $
 */
@Provider
class ViewWriter[T] extends MessageBodyWriter[View[T]] {
  import ViewWriter._

  @Context
  protected var uriInfo: UriInfo = _
  @Context
  protected var _servletContext: ServletContext = _
  @Context
  protected var request: HttpServletRequest = _
  @Context
  protected var response: HttpServletResponse = _

  protected var errorUris: List[String] = ServletHelper.errorUris()

  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[View[T]].isAssignableFrom(aClass)
  }

  def getSize(view: View[T], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L


  def writeTo(view: View[T], aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, httpHeaders: MultivaluedMap[String, Object], out: OutputStream): Unit = {
    def render(template: String) = TemplateEngineServlet.render(template, engine, servletContext, request, response)

    try {
      val template = view.uri
      finder.findTemplate(template) match {
        case Some(name) =>
          info("Attempting to generate View for %s", name)
          // Ensure headers are committed
          //out.flush()
          view.model match {
            case Some(it) => request.setAttribute("it", it)
            case _ =>
          }
          render(name)

        case _ =>
          throw new ResourceNotFoundException(template)
      }
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
          throw createContainerException(e)
        }
    }
  }

  protected lazy val finder = new TemplateFinder(engine)


  protected def engine = ServletTemplateEngine(servletContext)

  /**
   * Returns the servlet context injected by JAXRS
   */
  protected def servletContext: ServletContext = {
    if (_servletContext == null) {
      throw new IllegalArgumentException("servletContext not injected")
    }
    _servletContext
  }

  protected def createContainerException(e: Exception) = {
    new WebApplicationException(e)
  }

}