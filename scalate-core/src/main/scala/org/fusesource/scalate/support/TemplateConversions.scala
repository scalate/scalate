/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.support

import org.fusesource.scalate.util.Log

/**
 * A number of helper implicit conversions for use in templates
 */
object TemplateConversions {
  val log = Log(getClass); import log._


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
        debug(e, "Handling null pointer " + e)
        defaultValue
    }
  }


}