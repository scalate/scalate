package org.fusesource.scalate.squery.support

import xml.{Text, Elem, Node, NodeSeq}
import org.fusesource.scalate.squery.Transformer._

/**
 * Represents manipuluation rules
 *
 * @version $Revision: 1.1 $
 */
trait Rule {
  def transform(node: Node): NodeSeq
}

case class ReplaceRule(fn: (Node) => NodeSeq) extends Rule {
  def transform(node: Node) = fn(node)
}

case class ReplaceContentRule(fn: () => NodeSeq) extends Rule {
  def transform(node: Node) = node match {
    case e: Elem =>
      val contents = fn()
      println("Replacing content = " + contents)
      replaceContent(e, contents)
    case n => n
  }
}

case class SetAttributeRule(name: String, fn: (Node) => String) extends Rule {
  def transform(node: Node) = node match {
    case e: Elem =>
      val value = fn(e)
      println("Setting attribute " + name + " to " + value)
      setAttribute(e, name, value)

    case n => n
  }
}
