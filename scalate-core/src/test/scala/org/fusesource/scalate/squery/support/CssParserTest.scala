package org.fusesource.scalate.squery.support

import _root_.org.fusesource.scalate.FunSuiteSupport
import org.fusesource.scalate.squery.Selector


class CssParserTest extends FunSuiteSupport {
  val parser = new CssParser

  val xml = <table id="t1" class="people">
    <tr id="tr1">
      <td class="person">Hey</td>
    </tr>
  </table>


  assertSelector("table")
  assertSelector("table#t1")
  assertSelector("#t1")
  assertSelector(".people")
  assertSelector("table.people")

  def assertSelector(css: String, expected: Boolean = true): Unit = {
    test(css) {
      val selector = Selector(css)
      println("testing selector: " + selector)
      expect(expected) {selector.matches(xml, Nil)}
    }
  }
}