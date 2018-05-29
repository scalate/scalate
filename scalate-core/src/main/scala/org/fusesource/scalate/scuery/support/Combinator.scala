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

import org.fusesource.scalate.scuery._

/**
 * @version $Revision : 1.1 $
 */

abstract class Combinator() {

  /**
   * Returns the inner most selector; the selector on the RHS of the selector
   */
  def childSelector: Selector

  /**
   * Creates a selector using this combinator with the given ancestorSelector
   * which is on the left hand side of the combinator expression
   */
  def combinatorSelector(ancestorSelector: Selector): Selector
}

/**
 * Represents selector: E F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#descendant-combinators">description</a>
 */
case class DescendantCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(ancestorSelector: Selector) = DescendantSelector(childSelector, ancestorSelector)
}

/**
 * Represents selector: E &gt; F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#child-combinators">description</a>
 */
case class ChildCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(ancestorSelector: Selector) = ChildSelector(childSelector, ancestorSelector)
}

/**
 * Represents selector: E + F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#adjacent-sibling-combinators">description</a>
 */
case class AdjacentSiblingdCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(ancestorSelector: Selector) = AdjacentSiblingSelector(childSelector, ancestorSelector)
}

/**
 * Represents selector: E ~ F
 *
 * See the <a href"http://www.w3.org/TR/css3-selectors/#general-sibling-combinators">description</a>
 */
case class GeneralSiblingCombinator(childSelector: Selector) extends Combinator {

  def combinatorSelector(ancestorSelector: Selector) = GeneralSiblingSelector(childSelector, ancestorSelector)
}
