package org.fusesource.scalate.util

/**
 * @version $Revision: 1.1 $
 */

object Constraints {

  /**
   * Asserts that the given value has been injected
   * 
   * @throws IllegalArgumentException if the value has not been injected
   */
  def assertInjected[T](value: T, name: String): T = {
    assertNotNull(value, name, "has not been injected")
  }

  /**
   * Asserts that the given value is not null
   * 
   * @throws IllegalArgumentException if the value is null
   */
  def assertNotNull[T](value: T, name: String, reason: String = "should not be null"): T = {
    if (value == null) {
      throw new IllegalArgumentException(name + " " + reason)
    }
    else {
      value
    }
  }
}