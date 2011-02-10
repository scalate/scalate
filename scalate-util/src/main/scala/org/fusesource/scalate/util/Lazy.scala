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
 * Represents a lazily loaded property
 *
 * @version $Revision : 1.1 $
 */
class Lazy[T](thunk: => T) {
  type OwnerType = Unit

  private var _value: T = _
  private var _evaluated: Boolean = false

  def evaluated = _evaluated

  def value: T = {
    if (!_evaluated) {
      _value = thunk
      _evaluated = true
    }
    _value
  }

  def apply(): T = value

  def unapply(): Option[T] = Some(value)


  /**
   * Set the field to the value
   */
  def set(value: T): T = {
    _value = value
    _value
  }

  def :=[Q <% T](v: Q): T = {
    set(v)
  }

  def :=(v: T): T = {
    set(v)
  }

  /**
   * Assignment from the underlying type.  It's ugly, but:<br />
   * field() = new_value <br />
   * field set new_value <br />
   * field.set(new_value) <br />
   * are all the same
   */
  def update[Q <% T](v: Q) {
    this.set(v)
  }

  def apply[Q <% T](v: Q): OwnerType = {
    this.set(v)
    fieldOwner
  }

  def apply(v: T): OwnerType = { // issue 154
    this.set(v)
    fieldOwner
  }

  def fieldOwner: OwnerType = {}
}