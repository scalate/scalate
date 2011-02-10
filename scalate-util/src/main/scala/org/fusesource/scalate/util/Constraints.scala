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