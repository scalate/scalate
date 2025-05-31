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
package org.fusesource.scalate.scuery

import org.fusesource.scalate.scuery.support._

import scala.xml.{ Elem, Node, NodeSeq }

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
   * Returns a selector which returns the children of the given selector
   */
  def children(selector: Selector) = ChildrenSelector(selector)

  def pseudoSelector(identifier: String): Selector = identifier match {
    case "root" => RootSelector
    case "first-child" => FirstChildSelector
    case "last-child" => LastChildSelector
    case _ => throw new IllegalArgumentException("pseudo :" + identifier + " not supported")
  }

  def pseudoFunction(expression: AnyRef): Selector = throw new IllegalArgumentException("pseudo function :" + expression + " not supported")

  def pseudoFunction(name: String, counter: NthCounter): Selector = name match {
    case "nth-child" => NthChildSelector(counter)
    case _ => throw new IllegalArgumentException("pseudo function :" + name + " not supported")
  }

}

trait Selector {
  def matches(node: Node, ancestors: Seq[Node]): Boolean

  def filter(nodes: NodeSeq, ancestors: Seq[Node] = Nil): NodeSeq = {
    val rc = nodes.flatMap(filterNode(_, ancestors))
    if (rc.size == 1 && rc.head.isInstanceOf[Elem]) {
      rc.head.asInstanceOf[Elem]
    } else {
      rc
    }
  }

  protected def filterNode(n: Node, ancestors: Seq[Node]): NodeSeq = {
    if (matches(n, ancestors)) { n }
    else {
      n.child.flatMap {
        c => filterNode(c, n +: ancestors)
      }
    }
  }

  protected def attrEquals(e: Elem, name: String, value: String): Boolean = e.attribute(name) match {
    case Some(n) => n.toString == value
    case _ => false
  }

  /**
   * Returns the child elements of the given node
   */
  protected def childElements(node: Node): collection.Seq[Node] = node.child.filter(_.isInstanceOf[Elem])

  /**
   * Returns the child elements of the immediate ancestor
   */
  protected def ancestorChildElements(ancestors: Seq[Node]): collection.Seq[Node] =
    if (ancestors.isEmpty)
      Nil
    else
      childElements(ancestors.head)

}
