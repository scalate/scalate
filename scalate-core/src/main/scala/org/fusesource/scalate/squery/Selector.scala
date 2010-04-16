package org.fusesource.scalate.squery

import support.{CssParser, Combinator}
import xml.{Elem, Node}
import org.w3c.dom.Attr

/**
 * Implements CSS style selectors
 *
 * @version $Revision : 1.1 $
 */
object Selector {
  
  def apply(selector: String): Selector = {
    val parser = new CssParser
    parser.parseSelector(selector)
  }

  /**
   * Converts a list of selectors to a single selector on an element
   */
  def apply(selectors: Seq[Selector]): Selector = selectors match {
    case s :: Nil => s
    case s :: _ => CompositeSelector(selectors)
    case _ => AnyElementSelector()
  }

  /**
   * Converts a selector and a list of combinators into a single Selector which is capable of evaluating
   * itself from right to left on the current node
   */
  def apply(selector: Selector, combinators: Seq[Combinator]): Selector = combinators match {
    // if we had
    // a, (c1, b), (c2, c)
    // then we should create a selector
    // of c which then uses c2.selector(b, c1.selector(a))

    case Nil => selector
    case h :: Nil => h.combinatorSelector(selector) 
    case h :: xs => apply(h.combinatorSelector(selector), xs) 
  }

  /**
   * Returns a selector which returns the childen of the given selector
   */
  def children(selector: Selector) = ChildrenSelector(selector)


  def pseudoSelector(identifier: String): Selector = throw new IllegalArgumentException("pseudo :" + identifier + " not supported")
  def pseudoFunction(expression: AnyRef): Selector = throw new IllegalArgumentException("pseudo expression :" + expression + " not supported")

}

trait Selector {
  def matches(node: Node, parents: Seq[Node]): Boolean

  protected def attrEquals(e: Elem, name: String, value: String) = e.attribute(name) match {
    case Some(n) => n.toString == value
    case _ => false
  }
}

case class ClassSelector(className: String) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      attrEquals(e, "class", className)
    case _ => false
  }
}

case class IdSelector(className: String) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      attrEquals(e, "id", className)
    case _ => false
  }
}

case class ElementNameSelector(name: String) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      e.label == name
    case _ => false
  }
}

case class AttributeNameSelector(name: String) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Attr =>
      e.label == name
    case _ => false
  }
}

case class NamespacePrefixSelector(prefix: String) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node.prefix == prefix

}

case class AnyElementSelector() extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem => true
    case _ => false
  }
}

case class CompositeSelector(selectors: Seq[Selector]) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = selectors.find(!_.matches(node, parents)).isEmpty
}

case class ChildrenSelector(selector: Selector) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = parents match {
    case parent :: xs =>
      selector.matches(parent, xs)
    case _ => false
  }
}

case class NotSelector(selector: Selector) extends Selector {
  def matches(node: Node, parents: Seq[Node]) =  !selector.matches(node, parents)
}

object AnySelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) =  true
}
