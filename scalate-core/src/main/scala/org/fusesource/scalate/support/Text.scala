package org.fusesource.scalate.support

import scala.util.parsing.input.Positional

/**
 * Is a String with positioning information
 */
case class Text(value: String) extends Positional {

  def +(other: String) = Text(value + other).setPos(pos)

  def +(other: Text) = Text(value + other.value).setPos(pos)

  def trim = Text(value.trim).setPos(pos)

  def replaceAll(x: String, y: String) = Text(value.replaceAll(x, y)).setPos(pos)

  def isEmpty = value.length == 0

  def isWhitespace: Boolean = value.trim.length == 0

  override def equals(obj: Any) = obj match {
    case t: Text => t.value == value
    case _ => false
  }

  override def hashCode = value.hashCode

  override def toString = value

}
