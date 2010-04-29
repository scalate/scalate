package org.fusesource.scalate.scuery.support

import org.fusesource.scalate.scuery.Selector
import xml.{Attribute, Elem, Node, NodeSeq}

/**
 * Matches if the CSS class attribute contains the given class name word
 */
case class ClassSelector(className: String) extends Selector {
  private val matcher = IncludesMatch(className)

  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      e.attribute("class") match {
        case Some(nodes) => matcher.matches(nodes)
        case _ => false
      }
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

/**
 * Matches the current element if it has an attribute name
 */
case class AttributeNameSelector(name: String, matcher: Matcher) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      e.attribute(name) match {
        case Some(ns) => matcher.matches(ns)
        case _ => false
      }

    case _ => false
  }
}
/**
 * Matches the current element if it has a namespaced attribute name
 */
case class NamespacedAttributeNameSelector(name: String, prefix: String, matcher: Matcher) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      val uri = e.scope.getURI(prefix)
      if (uri != null) {
        e.attribute(uri, name) match {
          case Some(ns) => matcher.matches(ns)
          case _ => false
        }
      }
    else {
        false
      }

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

object AnyElementSelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem => true
    case _ => false
  }
}

// Combinators
//-------------------------------------------------------------------------


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


// Pseudo selectors
//-------------------------------------------------------------------------


object RootSelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      parents.isEmpty
    case _ => false
  }
}

object FirstChildSelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      parentChildElements(parents).headOption match {
        case Some(n) => n == e
        case _ => false
      }
    case _ => false
  }
}

object LastChildSelector extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      parentChildElements(parents).lastOption match {
        case Some(n) => n == e
        case _ => false
      }
    case _ => false
  }

}

case class NthChildSelector(counter: NthCounter) extends Selector {
  def matches(node: Node, parents: Seq[Node]) = node match {
    case e: Elem =>
      val idx = parentChildElements(parents).indexOf(node)
      counter.matches(idx)
    case _ => false
  }
}

/**
 * Used for the <a href="http://www.w3.org/TR/css3-selectors/#nth-child-pseudo">nth</a>
 * calculations representing an + b
 */
case class NthCounter(a: Int, b: Int) {
  def matches(idx: Int): Boolean = {
    if (idx < 0)
      false
    else {
      val oneIdx = idx + 1
      if (a == 0)
        oneIdx == b
      else
        oneIdx % a == b
    }

  }
}

object OddCounter extends NthCounter(2, 1)
object EvenCounter extends NthCounter(2, 0)