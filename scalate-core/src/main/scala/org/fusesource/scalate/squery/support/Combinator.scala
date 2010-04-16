package org.fusesource.scalate.squery.support

import org.fusesource.scalate.squery.Selector
import collection.Seq
import xml.Node

/**
 * @version $Revision : 1.1 $
 */

abstract class Combinator() {

  /**
   * Returns the inner most selector; the selector on the RHS of the selector
   */
  def childSelector: Selector

  /**
   * Creates a selector using this combinator with the given parentSelector
   * which is on the left hand side of the combinator expression
   */
  def combinatorSelector(parentSelector: Selector): Selector
}

/**
 * Represents selector: E &gt; F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#child-combinators">description</a>
 */
case class ChildCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(parentSelector: Selector): Selector = new Selector {
    def matches(node: Node, parents: Seq[Node]) = {
      if (childSelector.matches(node, parents)) {
        // lets apply the parentSelector to the immediate parent
        parents match {
          case h :: xs => parentSelector.matches(h, xs)
          case _ => false
        }
      }
      else {
        false
      }
    }
  }
}

/**
 * Represents selector: E + F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#adjacent-sibling-combinators">description</a>
 */
case class AdjacentSiblingdCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(parentSelector: Selector): Selector = new Selector {
    def matches(node: Node, parents: Seq[Node]) = {
      if (childSelector.matches(node, parents)) {
        // lets find immediate
        // lets apply the parentSelector to the immediate parent
        parents match {
          case h :: xs =>
            // find the index of node in h
          val children = h.child
          val idx = children.indexOf(node)
          if (idx <= 0) {
            false
          }
          else {
            parentSelector.matches(children(idx - 1), xs)
          }

          case _ => false
        }
      }
      else {
        false
      }
    }
  }
}

/**
 * Represents selector: E ~ F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#general-sibling-combinators">description</a>
 */
case class GeneralSiblingCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(parentSelector: Selector): Selector = new Selector {
    def matches(node: Node, parents: Seq[Node]) = {
      if (childSelector.matches(node, parents)) {
        // lets find immediate
        // lets apply the parentSelector to the immediate parent
        parents match {
          case h :: xs =>
            // find the index of node in h
          val children = h.child
          val idx = children.indexOf(node)
          if (idx <= 0) {
            false
          }
          else {
            val preceding = children.slice(0, idx).reverse
            preceding.find(parentSelector.matches(_, xs)).isDefined
          }
          case _ => false
        }
      }
      else {
        false
      }
    }
  }
}
