package org.fusesource.scalate.scass

import _root_.org.fusesource.scalate.util.Logging
import xml.{Elem, Node, NodeSeq}
import collection.mutable.{HashMap, ListBuffer}


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

  def transform(nodes: NodeSeq): NodeSeq = {
    nodes.flatMap(transformNode(_))
  }

  protected def transformNode(node: Node): NodeSeq = {
    println("Transforming node: " + node)
    val keys = _rules.filterKeys(_.matches(node))
    val size = keys.size
    if (size == 0) {
      node match {
        case e: Elem => replaceContent(e, transform(e.child))

      }
    }
    else {
      if (size > 1) {
        warn("Too many matching rules! " + keys)
      }
      keys.valuesIterator.next.transform(node)
    }
  }

  class RuleFactory(selector: Selector) {
    def content(fn: () => NodeSeq): Unit = {
      addRule(selector, new ReplaceContentRule(fn))
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