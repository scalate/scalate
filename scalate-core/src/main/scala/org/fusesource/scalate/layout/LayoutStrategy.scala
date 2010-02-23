package org.fusesource.scalate.layout

import org.fusesource.scalate.{RenderContext, Template}

/**
 * @version $Revision : 1.1 $
 */

trait LayoutStrategy {
  def layout(template: Template, context: RenderContext): Unit
}

class NullLayoutStrategy extends LayoutStrategy {
  def layout(template: Template, context: RenderContext) = template.render(context)
}