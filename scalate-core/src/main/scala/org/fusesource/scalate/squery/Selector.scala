package org.fusesource.scalate.squery

import support._
import xml.{Elem, Node, NodeSeq}
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

  implicit def toSXml(node: Node) = SXml(node)

  implicit def toSXml(nodes: NodeSeq) = SXml(nodes)

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

/**
 * A helper class to pimp Scala's XML support to add easy SQuery filtering
 * so that you can perform a CSS3 selector on a {@link Node} or {@link NodeSeq}
 * via <code>xml.$("someSelector")</code>
 */
case class SXml(nodes: NodeSeq) {
  def $(cssSelector: String): NodeSeq = $(Selector(cssSelector))

  def $(selector: Selector): NodeSeq = {
    nodes.flatMap(filter(_, Nil, selector))
  }

  protected def filter(n: Node, parents: Seq[Node], s: Selector): NodeSeq = {
    if (s.matches(n, parents))
      {n}
    else {
      n.child.flatMap {
        c => filter(c, n +: parents, s)
      }
    }
  }
}
