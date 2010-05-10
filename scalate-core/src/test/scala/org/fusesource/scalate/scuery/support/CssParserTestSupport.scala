package org.fusesource.scalate.scuery.support

import _root_.org.fusesource.scalate.FunSuiteSupport
import org.fusesource.scalate.scuery.Selector
import org.fusesource.scalate.scuery.XmlHelper._
import xml.{Elem, Node, NodeSeq}

abstract class CssParserTestSupport extends FunSuiteSupport {
  var parser = new CssParser

  def xml: Node

  def assertFilter(selector: String, expected: NodeSeq): Unit = {
    test("assertFilter: " + selector) {
      val actual = xml.$(selector)

      debug("filtering selector: " + selector + " expected: " + expected + " actual: " + actual)
      expect(expected) {actual}
    }
  }

  def assertMatches(css: String, node: Node): Unit = assertSelector("assertMatches ", css, node, true)

  def assertNotMatches(css: String, node: Node): Unit = assertSelector("assertNotMatches ", css, node, false)

  def assertSelector(message: String, css: String, node: Node, expected: Boolean): Unit = {
    test(message + css + " on " + summary(node)) {
      val selector = Selector(css)
      val ancestors = ancestorsOf(node)
      debug("testing selector: " + selector + " on " + summary(node) + " with ancestors: " + summary(ancestors))
      expect(expected) {selector.matches(node, ancestors)}
    }
  }

  def ancestorsOf(node: Node, ancestor: Node = xml): Seq[Node] = {
    def findChild(node: Node, ancestor: Node): Option[Seq[Node]] = {
      if (node == ancestor) {
        Some(Nil)
      }
      else if (ancestor.contains(node)) {
        Some(ancestor :: Nil)
      } else {
        var a: Option[Seq[Node]] = None
        for (c <- ancestor.child if a.isEmpty) {
          a = findChild(node, c)
        }
        a match {
          case Some(l) => Some(l ++ ancestor)
          case _ => a
        }
      }
    }
    findChild(node, ancestor).getOrElse(Nil)
  }

  protected def summary(node: Node): String = node match {
    case e: Elem => replaceContent(e, Nil).toString
    case _ => node.toString
  }

  protected def summary(nodes: NodeSeq): String = nodes.map(summary(_)).mkString(" ")
}