package org.fusesource.scalate

/**
 * Created by IntelliJ IDEA.
 * User: chirino
 * Date: Feb 1, 2010
 * Time: 10:24:35 AM
 * To change this template use File | Settings | File Templates.
 */

trait RenderContext {

  /**
   * Renders the provided value and inserts it into the final rendered document.
   */
  def <<(value: Any): Unit

  /**
   * Renders the provided value, sanitizes any XML special characters and inserts
   * it into the final rendered document.
   */
  def <<<(value: Any): Unit

  /**
   * Renders a value into a string.
   */
  def render(value: Any): String;

  /**
   * Gets the value of a template variable binding
   */
  def binding(name:String): Option[Any];

  /**
   * Sets the value of a template variable binding
   */
  def binding(name:String, value:Option[Any]): Unit;

  /**
   * Evaluates the specified body capturing any output written to this context
   * during the evaluation
   */
  def capture(body: => Unit): String;

  /**
   * renders and inserts another template
   */
  def include(path: String): Unit;

  implicit def body(body: => Unit): () => String = {
    () => {
      capture(body)
    }
  }

}