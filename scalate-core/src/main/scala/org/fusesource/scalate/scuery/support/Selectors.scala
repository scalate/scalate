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
package org.fusesource.scalate.scuery.support

import org.fusesource.scalate.scuery.Selector
import xml.{ Attribute, Elem, Node, NodeSeq }

/**
 * Matches if the CSS class attribute contains the given class name word
 */
case class ClassSelector(className: String) extends Selector {
  private val matcher = IncludesMatch(className)

  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      e.attribute("class") match {
        case Some(nodes) => matcher.matches(nodes)
        case _ => false
      }
    case _ => false
  }
}

case class IdSelector(className: String) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      attrEquals(e, "id", className)
    case _ => false
  }
}

case class ElementNameSelector(name: String) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      e.label == name
    case _ => false
  }
}

/**
 * Matches the current element if it has an attribute name
 */
case class AttributeNameSelector(name: String, matcher: Matcher) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
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
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      val uri = e.scope.getURI(prefix)
      if (uri != null) {
        e.attribute(uri, name) match {
          case Some(ns) => matcher.matches(ns)
          case _ => false
        }
      } else {
        false
      }

    case _ => false
  }
}

case class NamespacePrefixSelector(prefix: String) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = {
    // lets not compare prefixes, as we could have many prefixes mapped to the same URI
    // so lets compare the URI of the node to the URI of the prefix in scope on the node
    val boundUrl = node.scope.getURI(prefix)
    boundUrl != null && node.namespace == boundUrl
  }
}

object NoNamespaceSelector extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node.namespace == null
}

case class AnyElementSelector() extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem => true
    case _ => false
  }
}

case class CompositeSelector(selectors: Seq[Selector]) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = selectors.find(!_.matches(node, ancestors)).isEmpty
}

case class ChildrenSelector(selector: Selector) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = ancestors match {
    case ancestor :: xs =>
      selector.matches(ancestor, xs)
    case _ => false
  }
}

case class NotSelector(selector: Selector) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = !selector.matches(node, ancestors)
}

object AnySelector extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = true
}

object AnyElementSelector extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
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
case class ChildSelector(childSelector: Selector, ancestorSelector: Selector) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = {
    !ancestors.isEmpty && childSelector.matches(node, ancestors) && ancestorSelector.matches(ancestors.head, ancestors.tail)
  }
}

/**
 * Represents selector: E F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#descendant-combinators">description</a>
 */
case class DescendantSelector(childSelector: Selector, ancestorSelector: Selector) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = {
    !ancestors.isEmpty && childSelector.matches(node, ancestors) && matchAncestor(ancestors.head, ancestors.tail)
  }

  /**
   * recursively match the ancestor selector until we have no more ancestors
   */
  protected def matchAncestor(node: Node, ancestors: Seq[Node]): Boolean = {
    ancestorSelector.matches(node, ancestors) || (!ancestors.isEmpty && matchAncestor(ancestors.head, ancestors.tail))
  }
}

/**
 * Represents selector: E + F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#adjacent-sibling-combinators">description</a>
 */
case class AdjacentSiblingSelector(childSelector: Selector, ancestorSelector: Selector) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = {
    if (!ancestors.isEmpty && childSelector.matches(node, ancestors)) {
      // lets find immediate
      // lets apply the ancestorSelector to the immediate ancestor

      // find the index of node in ancestors children
      val h = ancestors.head
      val xs = ancestors.tail
      val children = h.child
      val idx = children.indexOf(node)
      idx > 0 && ancestorSelector.matches(children(idx - 1), xs)
    } else {
      false
    }
  }
}

/**
 * Represents selector: E ~ F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#general-sibling-combinators">description</a>
 */
case class GeneralSiblingSelector(childSelector: Selector, ancestorSelector: Selector) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = {
    if (!ancestors.isEmpty && childSelector.matches(node, ancestors)) {
      // lets find immediate
      // lets apply the ancestorSelector to the immediate ancestor

      // find the index of node in ancestors children
      val h = ancestors.head
      val xs = ancestors.tail

      val children = h.child
      val idx = children.indexOf(node)
      idx > 0 && children.slice(0, idx).reverse.find(ancestorSelector.matches(_, xs)).isDefined
    } else {
      false
    }
  }
}

// Pseudo selectors
//-------------------------------------------------------------------------

object RootSelector extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      ancestors.isEmpty
    case _ => false
  }
}

object FirstChildSelector extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      ancestorChildElements(ancestors).headOption match {
        case Some(n) => n == e
        case _ => false
      }
    case _ => false
  }
}

object LastChildSelector extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      ancestorChildElements(ancestors).lastOption match {
        case Some(n) => n == e
        case _ => false
      }
    case _ => false
  }

}

case class NthChildSelector(counter: NthCounter) extends Selector {
  def matches(node: Node, ancestors: Seq[Node]) = node match {
    case e: Elem =>
      val idx = ancestorChildElements(ancestors).indexOf(node)
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