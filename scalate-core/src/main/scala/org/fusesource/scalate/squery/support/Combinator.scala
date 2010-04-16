package org.fusesource.scalate.squery.support

import collection.Seq
import xml.Node
import org.fusesource.scalate.squery.{GeneralSiblingSelector, AdjacentSiblingSelector, ParentSelector, Selector}

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

  def combinatorSelector(parentSelector: Selector) = new ParentSelector(childSelector, parentSelector)
}

/**
 * Represents selector: E + F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#adjacent-sibling-combinators">description</a>
 */
case class AdjacentSiblingdCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(parentSelector: Selector) = new AdjacentSiblingSelector(childSelector, parentSelector)
}

/**
 * Represents selector: E ~ F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#general-sibling-combinators">description</a>
 */
case class GeneralSiblingCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(parentSelector: Selector) = new GeneralSiblingSelector(childSelector, parentSelector)
}
