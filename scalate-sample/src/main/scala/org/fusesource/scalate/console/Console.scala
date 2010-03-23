package org.fusesource.scalate.console

import _root_.java.io.FileWriter
import _root_.javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import _root_.javax.servlet.ServletContext
import _root_.javax.ws.rs._
import _root_.org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine}
import _root_.org.fusesource.scalate.util.Constraints._
import _root_.org.fusesource.scalate.util.IOUtil
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


  @POST
  @Path("createTemplate")
  def createTemplate(@FormParam("newTemplateName") newTemplateName: String,
                     @FormParam("archetype") archetype: String,
                     @FormParam("resourceClass") resourceClassName: String) = {

    assertNotNull(newTemplateName, "name")
    assertNotNull(archetype, "archetype")
    assertNotNull(resourceClassName, "resourceClass")

    // lets evaluate the archetype and write the output to the given template name
    val fileName = servletContext.getRealPath(newTemplateName)

    println("About to render archetype: " + archetype + " and generate: " + fileName)

    if (fileName == null) {
      throw new IllegalArgumentException("Could not deduce real file name for: " + newTemplateName)
    }
    val engine = ServletTemplateEngine(servletContext)
    val context = new ServletRenderContext(engine, request, response, servletContext)

    // lets try load the resource class
    val resourceClass = try {
      Thread.currentThread.getContextClassLoader.loadClass(resourceClassName)
    }
    catch {
      case e : ClassNotFoundException => getClass.getClassLoader.loadClass(resourceClassName)
    }
    
    context.attributes("resourceType") = resourceClass
    context.attributes("layout") = ""

    // lets capture the output of rendering the context
    val text = context.capture {
      engine.layout(archetype, context)
    }

    IOUtil.writeText(fileName, text)
  }

  @POST
  @Path("invalidateCachedTemplates")
  def invalidateCachedTemplates() = {
    println("clearing template cache")
    val engine = ServletTemplateEngine(servletContext)
    engine.invalidateCachedTemplates
  }

}