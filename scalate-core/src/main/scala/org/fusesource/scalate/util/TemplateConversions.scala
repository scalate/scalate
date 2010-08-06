package org.fusesource.scalate.util

import org.fusesource.scalate.support.{Elvis, MapEntry}

/**
 * A number of helper implicit conversions for use in templates
 */
object TemplateConversions extends Logging {


  /**
   * Provide access to the elvis operator so that we can use it to provide null handling nicely
   */
  implicit def anyToElvis(value: Any): Elvis = new Elvis(value)

  /**
   * Provide easy coercion from a Tuple2 returned when iterating over Java Maps to a Map.Entry type object
   */
  implicit def tuple2ToMapEntry[A, B](value: Tuple2[A, B]) = MapEntry[A, B](value._1, value._2)


  /**
   * A helper method for dealing with null pointers and also NullPointerException when navigating object expressions.
   *
   * If you are unsure if a value is null or a navigation through some object path is null then this function will
   * evaluate the expression, catch any NullPointerExceptions caused and return the default value.
   */
  def orElse[T](expression: => T, defaultValue: T) = {
    try {
      if (expression != null) {
        expression
      }
      else {
        defaultValue
      }
    } catch {
      case e: NullPointerException =>
        debug("Handling null pointer " + e, e)
        defaultValue
    }
  }


}