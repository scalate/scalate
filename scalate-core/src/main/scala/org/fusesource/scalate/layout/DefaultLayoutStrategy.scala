package org.fusesource.scalate.layout

import org.fusesource.scalate.{TemplateNotFoundException, RenderContext, Template, TemplateEngine}
import org.fusesource.scalate.util.Logging

/**
 * @version $Revision : 1.1 $
 */

class DefaultLayoutStrategy(val engine: TemplateEngine) extends LayoutStrategy with Logging {
  def layout(template: Template, context: RenderContext): Unit = {
    // lets capture the body and define any variables to be used for the layout
    val body = context.capture(template)

    // lets try find the default layout
    context.binding("layout") match {
      case Some(name: String) =>
        renderLayout(name, body, context)

      case _ =>
        try {
          renderLayout("WEB-INF/layouts/default.ssp", body, context)
        } catch {
          case e: TemplateNotFoundException =>
            try {
              renderLayout("WEB-INF/layouts/default.scaml", body, context)
            } catch {
              case e2: TemplateNotFoundException =>
                // lets not use layouts
                context << body
            }
        }
    }
  }

  def renderLayout(templateName: String, body: String, context: RenderContext): Unit = {
    fine("Attempting to load layout: " + templateName)

    val layoutTemplate = engine.load(templateName)
    context.binding("body", Some(body))
    layoutTemplate.render(context)
  }
}