package org.fusesource.scalate.scass

import xml.{Elem, Node}

/**
 * Implements CSS style selectors
 *
 * @version $Revision : 1.1 $
 */
object Selector {
  def apply(cssSelector: String): Selector = {
    if (cssSelector.startsWith(".")) {
      ClassSelector(cssSelector.stripPrefix("."))
    }
    else {
      throw new IllegalArgumentException("CSS syntax not supported: " + cssSelector)
    }
  }
}

trait Selector {
  def matches(node: Node): Boolean


  protected def attrEquals(e: Elem, name: String, value: String) = e.attribute(name) match {
    case Some(n) => println("attribute: " + name + " = " + n); n.toString == value
    case _ => false
  }
}

case class ClassSelector(className: String) extends Selector {
  def matches(node: Node) = node match {
    case e: Elem =>
      attrEquals(e, "class", className)
    case _ => false
  }
}

case class IdSelector(className: String) extends Selector {
  def matches(node: Node) = node match {
    case e: Elem =>
      attrEquals(e, "id", className)
    case _ => false
  }
}

