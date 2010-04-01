package org.fusesource.scalate

import _root_.org.fusesource.scalate.util.{RenderHelper, Logging}
import _root_.org.fusesource.scalate.support.{AttributesHashMap, Elvis}
import java.io._
import collection.mutable.Stack

/**
 * Default implementation of {@link RenderContext}
 */
class DefaultRenderContext(val engine: TemplateEngine, var out: PrintWriter) extends RenderContext with Logging {

  val attributes: AttributeMap[String,Any] = new AttributesHashMap[String, Any]() {
    update("context", DefaultRenderContext.this)
  }


  /**
   * Provide access to the elvis operator so that we can use it to provide null handling nicely
   */
  implicit def anyToElvis(value: Any): Elvis = new Elvis(value)


  /////////////////////////////////////////////////////////////////////
  //
  // RenderContext implementation
  //
  //////////////////////////////////x///////////////////////////////////

  def <<(v: Any): Unit = {
    out.print(value(v))
  }

  def <<<(v: Any): Unit = {
    out.print(RenderHelper.sanitize(value(v)))
  }


  private val outStack = new Stack[PrintWriter]

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def capture(body: => Unit): String = {
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      val u: Unit = body
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }

  /**
   * Evaluates the template capturing any output written to this page context during the body evaluation
   */
  def capture(template: Template): String = {
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      debug("rendering template " + template)
      template.render(this)
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }

}
