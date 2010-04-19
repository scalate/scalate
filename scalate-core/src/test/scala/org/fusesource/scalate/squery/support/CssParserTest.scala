package org.fusesource.scalate.squery.support

import _root_.org.fusesource.scalate.FunSuiteSupport
import org.fusesource.scalate.squery.Selector
import org.fusesource.scalate.squery.Selector._
import org.fusesource.scalate.squery.Transformer._
import xml.{Elem, Node, NodeSeq}

class CssParserTest extends FunSuiteSupport {
  val parser = new CssParser

  val cheese = <c:tr xmlns:c="http://apache.org/cheese"><blah/></c:tr>
  val a = <a href="http://scalate.fusesource.org/" title="Scalate" hreflang="en-US">Awesomeness</a>

  val xml = <table id="t1" class="people" summary="My Summary Notes">
    <tr id="tr1">
      <td class="person">Hey</td>
    </tr>
    {cheese}
    {a}
  </table>

  val tr1 = (xml \\ "tr")(0)
  val td1 = (xml \\ "td")(0)
  
  // simple stuff
  assertMatches("table", xml)
  assertMatches("table#t1", xml)
  assertMatches("#t1", xml)
  assertMatches(".people", xml)
  assertMatches("table.people", xml)

  assertMatches("*", td1)
  assertMatches("*.person", td1)
  assertMatches("*#tr1", tr1)

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

  // namespaces
  assertMatches("*|*", cheese)
  assertMatches("*|*", tr1)

  assertMatches("*|tr", cheese)
  assertMatches("*|tr", tr1)

  assertNotMatches("|tr", cheese)
  assertMatches("|tr", tr1)

  assertMatches("c|tr", cheese)
  assertNotMatches("c|tr", tr1)


  // attributes
  assertMatches("table[summary]", xml)
  assertMatches("[summary]", xml)
  assertNotMatches("[summary]", tr1)

  assertMatches("[summary = \"My Summary Notes\"]", xml)
  assertNotMatches("[summary = \"NotMatch\"]", xml)

  assertMatches("[summary=\"My Summary Notes\"]", xml)
  
  // ~= matches whole words
  assertMatches("[summary ~= \"My\"]", xml)
  assertMatches("[summary ~= \"Summary\"]", xml)
  assertMatches("[summary ~= \"Notes\"]", xml)
  assertNotMatches("[summary ~= \"My Summary\"]", xml) // can only filter on whole words

  assertMatches("[summary~=\"My\"]", xml)

  // |=
  assertMatches("[hreflang |= \"en\"]", a)
  assertMatches("[hreflang |= \"en-US\"]", a)
  assertNotMatches("[hreflang |= \"de\"]", a)

  // TODO regex bug!!!
  // not sure yet why the parser fails to parse this!
  //assertMatches("[hreflang|=\"en\"]", a)
  //assertMatches("[hreflang|=en]", a)

  assertMatches("[hreflang |= en]", a)
  assertMatches("[hreflang |= en-US]", a)

  // ^=
  assertMatches("[summary ^= \"My\"]", xml)
  assertMatches("[summary ^= \"My S\"]", xml)
  assertNotMatches("[summary ^= \"T\"]", xml)

  assertMatches("[summary^=\"My\"]", xml)

  // $=
  assertMatches("[summary $= \"Notes\"]", xml)
  assertMatches("[summary $= \"Summary Notes\"]", xml)
  assertNotMatches("[summary $= \"Cheese\"]", xml)

  assertMatches("[summary$=\"Notes\"]", xml)

  // *=
  assertMatches("[summary *= \"Sum\"]", xml)
  assertMatches("[summary *= \"Summary N\"]", xml)
  assertNotMatches("[summary *= \"Cheese\"]", xml)

  assertMatches("[summary*=\"Sum\"]", xml)


  // :not
  assertMatches("td:not(.food)", td1)
  assertNotMatches("td:not(.person)", td1)


  // filtering using the pimped API on scala Node*
  assertFilter(".person", td1)
  assertFilter("table td", td1)
  assertFilter("table tr td", td1)

  def assertFilter(selector: String, expected: NodeSeq): Unit = {
    test("assertFilter: " + selector) {
      val actual = xml.$(selector)

      println("filtering selector: " + selector + " expected: " + expected + " actual: " + actual)
      expect(expected) {actual}
    }
  }

  def assertMatches(css: String, node: Node): Unit = assertSelector("assertMatches ", css, node, true)

  def assertNotMatches(css: String, node: Node): Unit = assertSelector("assertNotMatches ", css, node, false)

  def assertSelector(message: String, css: String, node: Node, expected: Boolean): Unit = {
    test(message + css + " on " + summary(node)) {
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

  protected def summary(node: Node): String = node match {
    case e: Elem => replaceContent(e, Nil).toString
    case _ => node.toString
  }

  protected def summary(nodes: NodeSeq): String = nodes.map(summary(_)).mkString(" ")
}