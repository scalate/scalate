package org.fusesource.scalate.scuery.support

import xml.{Elem, Node, NodeSeq}
import org.fusesource.scalate.scuery.XmlHelper._
import org.fusesource.scalate.util.Logging

object Rule {
  /**
   * Combines multiple rules to a single rule
   */
  def apply(a: Rule, b: Rule): Rule = {
    if (a.order <= b.order) {
      CompositeRule(a, b)
    }
    else {
      CompositeRule(b, a)
    }
  }

  def apply(rules: Iterator[Rule]): Rule = apply(rules.toSeq)
  
  def apply(rules: Seq[Rule]): Rule = {
    if (rules.size < 2) {
      rules(0)
    }
    else {
      val list = rules.sortWith(_.order < _.order)
      list.tail.foldLeft(list.head)(Rule(_, _))
    }
  }
}

/**
 * Represents manipuluation rules
 *
 * @version $Revision : 1.1 $
 */
trait Rule extends Logging {
  def apply(node: Node): NodeSeq

  /**
   * Lets do simple rules first (like setting attributes, removing attributes), then changing contents
   * then finally completely transforming the node last
   */
  def order: Int = 0
}

case class CompositeRule(first: Rule, second: Rule) extends Rule {
  def apply(node: Node) = {
    first(node).flatMap {second(_)}
  }

  def toList: List[Rule] = toList(first) ::: toList(second)

  protected def toList(rule: Rule): List[Rule] = rule match {
    case c: CompositeRule => c.toList
    case _ => rule :: Nil
  }
}

case class ReplaceRule(fn: Node => NodeSeq) extends Rule {
  def apply(node: Node) = fn(node)

  override def order: Int = 100
}

case class ReplaceContentRule(fn: Node => NodeSeq) extends Rule {
  def apply(node: Node) = node match {
    case e: Elem =>
      val contents = fn(e)
      debug("Replacing content = " + contents)
      replaceContent(e, contents)
    case n => n
  }
}

case class SetAttributeRule(name: String, fn: (Node) => String) extends Rule {
  def apply(node: Node) = node match {
    case e: Elem =>
      val value = fn(e)
      debug("Setting attribute " + name + " to " + value)
      setAttribute(e, name, value)

    case n => n
  }

  override def order = -1
}
