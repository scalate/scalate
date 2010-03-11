package org.fusesource.scalate.layout

import org.fusesource.scalate.{ TemplateException, RenderContext, Template, TemplateEngine }
import org.fusesource.scalate.util.Logging

/**
 * The default implementation of <code>LayoutStrategy</code>.
 * 
 * <p>This implementation will first try to load a layout by using
 * the "layout" attribute of the given template. If the attribute
 * is not found then the "WEB-INF/layouts/default.ssp" and the
 * "WEB-INF/layouts/default.scaml" layouts are tried in that order.</p> 
 * 
 * @version $Revision : 1.1 $
 */
class DefaultLayoutStrategy(val engine: TemplateEngine) extends LayoutStrategy with Logging {
  
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
        for {
          _ <- tryLayout("WEB-INF/layouts/default.ssp", body, context)
          _ <- tryLayout("WEB-INF/layouts/default.scaml", body, context)
          _ <- noLayout(body, context)
        } { }
    }
  }
  
  private def renderLayout(layoutTemplate: String, body: String, context: RenderContext) {
      fine("Attempting to load layout: " + layoutTemplate)
      context.attributes("body") = body
      engine.load(layoutTemplate).render(context)
  }
  
  /* Returns Option so it can be used in a for comprehension. */
  private def tryLayout(layoutTemplate: String, body: String, context: RenderContext): Option[Boolean] = {
    try {
      renderLayout(layoutTemplate, body, context)
      None
    } catch {
      case e: TemplateException => Some(true)
    }
  }

  /* Returns Option so it can be used in a for comprehension. */
  private def noLayout(body: String, context: RenderContext) = {
    context << body
    None
  }
}
