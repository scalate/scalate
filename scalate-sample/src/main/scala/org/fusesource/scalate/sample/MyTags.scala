package org.fusesource.scalate.sample

import org.fusesource.scalate.TemplateContext

/**
 * @version $Revision : 1.1 $
 */

object MyTags {

  /**
   * This option hides the page context
   */
  def someLayout(body: () => String) = {
    val text = body()
    println("found text: " + text)
    "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }


  /**
   * Explicit version where you interact with the context parameter
   */
  def someLayoutUsingTemplateContext(context: TemplateContext)(body: => Unit) = {
    val text = context.evaluate(body)
    println("Evaluated text: " + text)
    context.write("<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>")
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