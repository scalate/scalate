package org.fusesource.scalate

import scala.collection.mutable.Map

/**
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
   * Access the attributes available in this context
   */
  def attributes : AttributeMap[String,Any]

  /**
   * Returns the attribute of the given type or a    { @link NoValueSetException } exception is thrown
   */
  def attribute[T](name: String): T = {
    val value = attributes.get(name)
    if (value.isDefined) {
      value.get.asInstanceOf[T]
    }
    else {
      throw new NoValueSetException(name)
    }
  }

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: T): T = {
    val value = attributes.get(name)
    if (value.isDefined) {
      value.get.asInstanceOf[T]
    }
    else {
      defaultValue
    }
  }

  def setAttribute[T](name: String, value: T): Unit = {
    attributes(name) = value
  }


  /**
   * Renders a value into a string.
   */
  def render(value: Any): String

  /**
   * Evaluates the specified body capturing any output written to this context
   * during the evaluation
   */
  def capture(body: => Unit): String

  /**
   * Evaluates the template capturing any output written to this page context during the body evaluation
   */
  def capture(template: Template): String

  /**
   * renders and inserts another template
   */
  def include(path: String): Unit

  implicit def body(body: => Unit): () => String = {
    () => {
      capture(body)
    }
  }

}