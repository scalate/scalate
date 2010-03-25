package org.fusesource.scalate.console

import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import _root_.javax.servlet.ServletContext
import _root_.javax.ws.rs._
import _root_.org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine}
import _root_.org.fusesource.scalate.util.Constraints._
import javax.ws.rs.core.Context

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
    println("clearing template cache")
    val engine = ServletTemplateEngine(servletContext)
    engine.invalidateCachedTemplates
  }
}