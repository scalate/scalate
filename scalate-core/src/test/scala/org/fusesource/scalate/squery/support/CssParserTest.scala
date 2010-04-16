package org.fusesource.scalate.squery.support

import _root_.org.fusesource.scalate.FunSuiteSupport
import org.fusesource.scalate.squery.Selector
import xml.{Node, NodeSeq}

class CssParserTest extends FunSuiteSupport {
  val parser = new CssParser

  val xml = <table id="t1" class="people">
    <tr id="tr1">
      <td class="person">Hey</td>
    </tr>
  </table>

  val tr1 = (xml \\ "tr") (0)
  val td1 = (xml \\ "td") (0)

  // simple stuff
  assertSelector("table", xml)
  assertSelector("table#t1", xml)
  assertSelector("#t1", xml)
  assertSelector(".people", xml)
  assertSelector("table.people", xml)

  // combinators
  assertSelector("tr > td", td1, tr1)

  def assertSelector(css: String, node: Node, parents: Seq[Node] = Nil, expected: Boolean = true): Unit = {
    test(css) {
      val selector = Selector(css)
      println("testing selector: " + selector + " on " + summary(node) + " with parents: " + summary(parents))
      expect(expected) {selector.matches(node, parents)}
    }
  }

  protected def summary(node: Node): String = node.label
  protected def summary(nodes: NodeSeq): String = nodes.map(summary(_)).mkString(" ")
}