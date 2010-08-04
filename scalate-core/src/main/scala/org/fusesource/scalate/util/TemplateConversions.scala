package org.fusesource.scalate.util

import org.fusesource.scalate.support.{Elvis, MapEntry}

/**
 * A number of helper implicit conversions for use in templates
 */
object TemplateConversions {


  /**
   * Provide access to the elvis operator so that we can use it to provide null handling nicely
   */
  implicit def anyToElvis(value: Any): Elvis = new Elvis(value)

  /**
   * Provide easy coercion from a Tuple2 returned when iterating over Java Maps to a Map.Entry type object
   */
  implicit def tuple2ToMapEntry[A,B](value: Tuple2[A,B]) = MapEntry[A,B](value._1, value._2)

}