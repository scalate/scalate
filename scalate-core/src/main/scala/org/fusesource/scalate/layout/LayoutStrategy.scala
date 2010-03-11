package org.fusesource.scalate.layout

import org.fusesource.scalate.{RenderContext, Template}

/**
 * A strategy for loading and applying layout templates.
 * 
 * @version $Revision : 1.1 $
 */
trait LayoutStrategy {
  
  /**
   * Render the specified template by decorating it with another "layout template".
   * 
   * @param template the template to render
   * @param context  the context that will be used for rendering the template
   */
  def layout(template: Template, context: RenderContext): Unit
}

/**
 * A <code>LayoutStrategy</code> that renders the given template without
 * using any layout. 
 */
class NullLayoutStrategy extends LayoutStrategy {
  def layout(template: Template, context: RenderContext) = template.render(context)
}