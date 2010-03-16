package org.fusesource.scalate

/**
 * @version $Revision: 1.1 $
 */
class Body(context: RenderContext, body: => Unit) {

  def capture = context.capture(body)

  def execute: Unit = body
}