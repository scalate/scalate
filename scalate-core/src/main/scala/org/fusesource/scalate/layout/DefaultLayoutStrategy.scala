package org.fusesource.scalate.layout

import org.fusesource.scalate.{ResourceNotFoundException, RenderContext, Template, TemplateEngine }
import org.fusesource.scalate.util.Logging

/**
 * The default implementation of <code>LayoutStrategy</code>.
 * 
 * <p>This implementation will first try to load a layout by using
 * the "layout" attribute of the given template.
 *
 * If the attribute is not found then these files will be searched
 *   * "WEB-INF/layouts/default.mustache"
 *   * "WEB-INF/layouts/default.scaml"
 *   * "WEB-INF/layouts/default.ssp"
 *
 * @version $Revision : 1.1 $
 */
class DefaultLayoutStrategy(val engine: TemplateEngine, val defaultLayouts: String*) extends LayoutStrategy with Logging {

  def layout(template: Template, context: RenderContext) {
    
    def isLayoutDisabled(layout: String) = layout.trim.isEmpty
    
    // lets capture the body to be used for the layout
    val body = context.capture(template)

    // lets try find the default layout
    context.attributes.get("layout") match {
      case Some(layout: String) =>
        if (isLayoutDisabled(layout))
          noLayout(body, context)
        else
          renderLayout(layout, body, context)

      case _ =>
        val layoutName = defaultLayouts.find(tryLayout(_, body, context))
        if (layoutName.isEmpty) {
          noLayout(body, context)
        }
    }
  }
  
  private def renderLayout(layoutTemplate: String, body: String, context: RenderContext) {
      debug("Attempting to load layout: " + layoutTemplate)
      context.attributes("body") = body
      context.attributes("scalateLayouts") = layoutTemplate :: context.attributeOrElse[List[String]]("scalateLayouts", List()) 

      engine.load(layoutTemplate).render(context)
      debug("layout completed of: " + layoutTemplate)
  }
  
  private def tryLayout(layoutTemplate: String, body: String, context: RenderContext): Boolean = {
    try {
      renderLayout(layoutTemplate, body, context)
      true
    } catch {
      case e: ResourceNotFoundException => debug("Caught: " + e, e)
        false
      case e: Exception => error("Unhandled: " + e, e)
        throw e
    }
  }

  /* Returns Option so it can be used in a for comprehension. */
  private def noLayout(body: String, context: RenderContext) = {
    context << body
    None
  }
}
