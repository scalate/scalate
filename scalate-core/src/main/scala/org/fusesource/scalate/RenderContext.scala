package org.fusesource.scalate

/**
 * Provides helper methods for rendering templates.
 * 
 * @see DefaultRenderContext
 * @see org.fusesource.scalate.servlet.ServletRenderContext
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
   * Returns the attribute of the given type or a {@link NoValueSetException} exception is thrown
   */
  def attribute[T](name: String): T = {
    attributes.get(name)
              .getOrElse(throw new NoValueSetException(name))
              .asInstanceOf[T]
  }

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: T): T = {
    attributes.get(name)
              .getOrElse(defaultValue)
              .asInstanceOf[T]
  }

  def setAttribute(name: String, value: Option[Any]) {
    value match {
      case Some(v) => attributes(name) = v
      case None    => attributes.remove(name)
    }
  }

  /**
   * Converts a value into a string, using the current locale for converting numbers and dates to a string.
   */
  def value(value: Any): String

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
   * Renders and inserts another template
   */
  def include(path: String): Unit

  implicit def body(body: => Unit): () => String = {
    () => {
      capture(body)
    }
  }
}
