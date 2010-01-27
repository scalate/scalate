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