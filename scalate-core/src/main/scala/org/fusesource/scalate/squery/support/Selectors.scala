package org.fusesource.scalate.squery.support

import org.fusesource.scalate.squery.Selector
import xml.{Attribute, Elem, Node, NodeSeq}

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
    case e: Attribute =>
      e.label == name
    case _ => false
  }
}

case class NamespacePrefixSelector(prefix: String) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = {
    // lets not compare prefixes, as we could have many prefixes mapped to the same URI
    // so lets compare the URI of the node to the URI of the prefix in scope on the node
    val boundUrl = node.scope.getURI(prefix)
    boundUrl != null && node.namespace == boundUrl
  }
}

object NoNamespaceSelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node.namespace == null
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
  def matches(node: Node, parents: Seq[Node]) = !selector.matches(node, parents)
}

object AnySelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) = true
}

/**
 * Represents selector: E &gt; F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#child-combinators">description</a>
 */
case class ChildSelector(childSelector: Selector, parentSelector: Selector) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = {
    !parents.isEmpty && childSelector.matches(node, parents) && parentSelector.matches(parents.head, parents.tail)
  }
}

/**
 * Represents selector: E F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#descendant-combinators">description</a>
 */
case class DescendantSelector(childSelector: Selector, parentSelector: Selector) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = {
    !parents.isEmpty && childSelector.matches(node, parents) && matchParent(parents.head, parents.tail)
  }

  /**
   * recursively match the parent selector until we have no more parents
   */
  protected def matchParent(node: Node, parents: Seq[Node]): Boolean = {
    parentSelector.matches(node, parents) || (!parents.isEmpty && matchParent(parents.head, parents.tail))
  }
}

/**
 * Represents selector: E + F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#adjacent-sibling-combinators">description</a>
 */
case class AdjacentSiblingSelector(childSelector: Selector, parentSelector: Selector) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = {
    if (!parents.isEmpty && childSelector.matches(node, parents)) {
      // lets find immediate
      // lets apply the parentSelector to the immediate parent

      // find the index of node in parents children
      val h = parents.head
      val xs = parents.tail
      val children = h.child
      val idx = children.indexOf(node)
      idx > 0 && parentSelector.matches(children(idx - 1), xs)
    }
    else {
      false
    }
  }
}

/**
 * Represents selector: E ~ F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#general-sibling-combinators">description</a>
 */
case class GeneralSiblingSelector(childSelector: Selector, parentSelector: Selector) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = {
    if (!parents.isEmpty && childSelector.matches(node, parents)) {
      // lets find immediate
      // lets apply the parentSelector to the immediate parent

      // find the index of node in parents children
      val h = parents.head
      val xs = parents.tail

      val children = h.child
      val idx = children.indexOf(node)
      idx > 0 && children.slice(0, idx).reverse.find(parentSelector.matches(_, xs)).isDefined
    }
    else {
      false
    }
  }
}
