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

  val tr1 = (xml \\ "tr")(0)
  val td1 = (xml \\ "td")(0)
  val td1Parents = tr1 :: xml :: Nil

  // simple stuff
  assertMatches("table", xml)
  assertMatches("table#t1", xml)
  assertMatches("#t1", xml)
  assertMatches(".people", xml)
  assertMatches("table.people", xml)

  // combinators
  assertMatches("tr > td", td1)
  assertMatches("tr td", td1)
  assertMatches("table td", td1)
  assertMatches("table tr td", td1)
  assertMatches("table tr .person", td1)
  assertMatches("table > tr > td", td1)
  assertMatches("table .person", td1)
  assertMatches("td.person", td1)
  assertMatches("tr .person", td1)
  assertMatches("tr > .person", td1)

  assertNotMatches("foo", td1)
  assertNotMatches(".foo", td1)
  assertNotMatches("#foo", td1)
  assertNotMatches("tr table td", td1)
  assertNotMatches("table tr tr td", td1)
  assertNotMatches("table table tr td", td1)
  assertNotMatches("table > td", td1)
  assertNotMatches("tr > table > td", td1)
  assertNotMatches("td > tr", td1)

  def assertMatches(css: String, node: Node): Unit = assertSelector(css, node, true)

  def assertNotMatches(css: String, node: Node): Unit = assertSelector(css, node, false)

  def assertSelector(css: String, node: Node, expected: Boolean = true): Unit = {
    test(css) {
      val selector = Selector(css)
      val parents = parentsOf(node)
      println("testing selector: " + selector + " on " + summary(node) + " with parents: " + summary(parents))
      expect(expected) {selector.matches(node, parents)}
    }
  }

  def parentsOf(node: Node, parent: Node = xml): Seq[Node] = {
    def findChild(node: Node, parent: Node): Option[Seq[Node]] = {
      if (node == parent) {
        Some(Nil)
      }
      else if (parent.contains(node)) {
        Some(parent :: Nil)
      } else {
        var a: Option[Seq[Node]] = None
        for (c <- parent.child if a.isEmpty) {
          a = findChild(node, c)
        }
        a match {
          case Some(l) => Some(l ++ parent)
          case _ => a
        }
      }
    }
    findChild(node, parent).getOrElse(Nil)
  }

  protected def summary(node: Node): String = node.label

  protected def summary(nodes: NodeSeq): String = nodes.map(summary(_)).mkString(" ")
}