package org.fusesource.scalate.scuery.support

import _root_.org.fusesource.scalate.FunSuiteSupport
import org.fusesource.scalate.scuery.Selector
import org.fusesource.scalate.scuery.Transformer._
import xml.{Elem, Node, NodeSeq}

abstract class CssParserTestSupport extends FunSuiteSupport {
  var parser = new CssParser

  def xml: Node

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