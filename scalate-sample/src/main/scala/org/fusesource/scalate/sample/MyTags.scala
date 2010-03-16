package org.fusesource.scalate.sample

import org.fusesource.scalate.{Body, RenderContext}

/**
 * @version $Revision : 1.1 $
 */

object MyTags {

  /**
   * Implicit version using an import
   */
  def someLayoutWithRenderContextImport(body: => Unit) = {
    val current = RenderContext()
    import current._
    val text = capture(body)
   "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * Implicit version where the current render context is used in the implementation
   */
  def someLayoutUsesRenderContext(body: => Unit) = {
    val context = RenderContext()
    val text = context.capture(body)
    context << ("<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>")
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
   * TODO not working yet!
   *
   * Takes a Body object
   */
  def someLayoutUsingBody(body: Body) = {
    val text = body.capture
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