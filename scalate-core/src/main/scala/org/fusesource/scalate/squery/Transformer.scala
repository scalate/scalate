package org.fusesource.scalate.squery

import _root_.org.fusesource.scalate.util.Logging
import collection.mutable.{HashMap, ListBuffer}
import text.Document
import xml.{Text, Elem, Node, NodeSeq}


object Transformer {
  def replaceContent(e: Elem, content: NodeSeq) = new Elem(e.prefix, e.label, e.attributes, e.scope, content: _*)
}

import Transformer._

/**
 * Allows simple XML replacement rules to be registered
 *
 * @version $Revision : 1.1 $
 */
class Transformer extends Logging {
  protected val _rules = new HashMap[Selector, Rule]

  def $(cssSelector: String): RuleFactory = $(Selector(cssSelector))

  def $(selector: Selector): RuleFactory = new RuleFactory(selector)

  def transform(nodes: NodeSeq, parents: Seq[Node] = Nil): NodeSeq = {
    nodes.flatMap(transformNode(_, parents))
  }

  protected def transformNode(node: Node, parents: Seq[Node]): NodeSeq = {
    val keys = _rules.filterKeys(_.matches(node, parents))
    val size = keys.size
    if (size == 0) {
      node match {
        case e: Elem => replaceContent(e, transform(e.child, parents ++ e))
        case d: Document => transform(d.child)
        case n => n
      }
    }
    else {
      if (size > 1) {
        warn("Too many matching rules! " + keys)
      }
      val rule = keys.valuesIterator.next
      rule.transform(node)
    }
  }

  class RuleFactory(selector: Selector) {
    def content: RuleFactory = new RuleFactory(selector)  // TODO use child

    /**
     * Sets the content of the matching element to the given set of markup 
     */
    def content_=(nodes: NodeSeq): Unit = {
      def fn(): NodeSeq = nodes
      addRule(selector, new ReplaceContentRule(fn))
    }

    /**
     * Sets the content of the matching element to the given text
     */
    def content_=(text: String): Unit = {
      content = Text(text)
    }
  }

  trait Rule {
    def transform(node: Node): NodeSeq
  }

  class ReplaceContentRule(fn: () => NodeSeq) extends Rule {
    def transform(node: Node) = node match {
      case e: Elem =>
        val content = fn()
        println("Replacing content = " + content)
        replaceContent(e, content)
      //new Elem(e.prefix, e.label, e.attributes, e.scope, content :_*)
      case n => n
    }
  }


  protected def addRule(selector: Selector, rule: Rule) = _rules(selector) = rule
}