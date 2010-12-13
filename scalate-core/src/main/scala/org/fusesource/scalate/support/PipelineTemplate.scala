package org.fusesource.scalate
package support

import filter.Filter

/**
 * Implements a Template using a list of filters
 */
class PipelineTemplate(pipeline: List[Filter], text: String) extends Template {

  def render(context: RenderContext) = {
    var rc = text
    for (f <- pipeline) {
      rc = f.filter(context, rc)
    }
    context << rc;
  }

  override def toString = "PipelineTemplate(pipeline=" + pipeline + ")"
}