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
    context.attributes.get("layout") match {
      case Some(name: String) =>
        val t = name.trim
        if (t.length == 0) {
          // lets not use layouts
          noLayout(body, template, context)
        }
        else {
          renderLayout(name, body, context)
        }

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
                noLayout(body, template, context)
            }
        }
    }
  }

  def renderLayout(templateName: String, body: String, context: RenderContext): Unit = {
    fine("Attempting to load layout: " + templateName)

    val layoutTemplate = engine.load(templateName)
    context.attributes("body") = body
    layoutTemplate.render(context)
  }

  def noLayout(body: String, template: Template, context: RenderContext): Unit = context << body
}