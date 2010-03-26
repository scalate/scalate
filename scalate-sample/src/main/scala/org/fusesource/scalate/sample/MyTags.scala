package org.fusesource.scalate.sample

import org.fusesource.scalate.RenderContext
import org.fusesource.scalate.RenderContext.capture

/**
 * @version $Revision : 1.1 $
 */

object MyTags {

  /**
   * Implicit version we import the capture method
   */
  def someLayoutWithImportedCapture(body: => Unit) = {
    val text = capture(body)
    "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * Implicit version using an import
   */
  def someLayoutWithRenderContextVariable(body: => Unit) = {
    val context = RenderContext()
    val text = context.capture(body)
   "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * Explicit version where you interact with the context parameter directly
   */
  def someLayoutWithRenderContextParam(context: RenderContext)(body: => Unit) = {
    val text = context.capture(body)
    context << ("<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>")
  }


  //-------------------------------------------------------------------------
  // TODO the following methods dont work!
  //-------------------------------------------------------------------------

  /**
   * TODO not working yet!
   *
   * This option hides the render context
   */
  def someLayout(body: () => String) = {
    val text = body()
    println("found text: " + text)
    "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * TODO Not working yet - we currently only support the () => String version
   */
  def someLayoutNotWorking(body: => String) = {
    val text = body
    println("found text: " + text)
    "<h3>Wrapped body</h3><p>" + body + "</p><h3>End of wrapped body</h3>"
  }
}