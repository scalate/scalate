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
package org.fusesource.scalate.console

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import _root_.javax.servlet.ServletContext
import _root_.javax.ws.rs._
import _root_.org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine}
import _root_.org.fusesource.scalate.util.Constraints._
import javax.ws.rs.core.Context
import org.fusesource.scalate.util.Log

object Console extends Log; import Console._

/**
 * The Scalate development console
 *
 * @version $Revision : 1.1 $
 */
@Path("/scalate")
class Console extends DefaultRepresentations {

  @Context
  var _servletContext: ServletContext = _
  @Context
  var _request: HttpServletRequest = _
  @Context
  var _response: HttpServletResponse = _

  def servletContext: ServletContext = assertInjected(_servletContext, "servletContext")

  def request: HttpServletRequest = assertInjected(_request, "request")

  def response: HttpServletResponse = assertInjected(_response, "response")

  def templateEngine = ServletTemplateEngine(servletContext)
  def renderContext = new ServletRenderContext(templateEngine, request, response, servletContext)


  @Path("archetypes/{name}")
  def archetype(@PathParam("name") name: String) = new ArchetypeResource(this, name)


  @POST
  @Path("invalidateCachedTemplates")
  def invalidateCachedTemplates() = {
    info("Clearing template cache")
    val engine = ServletTemplateEngine(servletContext)
    engine.invalidateCachedTemplates
  }
}