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

import _root_.org.fusesource.scalate.FunSuiteSupport
import xml.{ Text, Node, NodeSeq }
class RuleTest extends FunSuiteSupport {

  def fn1(node: Node): NodeSeq = Text("f1")
  def fn2(node: Node): String = "f2"

  test("rules are sorted in order when combined") {
    val rule = Rule(List(ReplaceContentRule(fn1), ReplaceRule(fn1), SetAttributeRule("foo", fn2), ReplaceContentRule(fn1), SetAttributeRule("bar", fn2), ReplaceRule(fn1)))

    assert(rule.isInstanceOf[CompositeRule])

    val rules = rule.asInstanceOf[CompositeRule].toList

    val setAttributeType = classOf[SetAttributeRule]
    val replaceContentType = classOf[ReplaceContentRule]
    val replaceType = classOf[ReplaceRule]

    assertTypes(rules, List(setAttributeType, setAttributeType, replaceContentType, replaceContentType, replaceType, replaceType))
  }

  def assertTypes(list: List[AnyRef], types: List[Class[_]]): Unit = {
    for ((t, i) <- types.zipWithIndex) {
      val v = list(i)
      logger.debug("item at " + i + " = " + v + " should be " + t)
      assertResult(t, "itemn at " + i) { v.getClass }
    }
  }

}
