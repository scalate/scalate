package org.fusesource.scalate.scuery.support

import _root_.org.fusesource.scalate.FunSuiteSupport
import xml.{Text, Elem, Node, NodeSeq}
import org.fusesource.scalate.util.Logging

class RuleTest extends FunSuiteSupport with Logging {

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
      debug("item at " + i + " = " + v + " should be " + t)
      expect(t, "itemn at " + i) {v.getClass}
    }
  }

}