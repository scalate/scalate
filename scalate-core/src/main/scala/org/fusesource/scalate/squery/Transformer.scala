package org.fusesource.scalate.squery

import _root_.org.fusesource.scalate.util.Logging
import _root_.org.fusesource.scalate.squery.support._
import collection.mutable.{HashMap, ListBuffer}
import text.Document
import xml.{Attribute, Text, Elem, Node, NodeSeq, Null}


object Transformer {
  def replaceContent(e: Elem, content: NodeSeq) = new Elem(e.prefix, e.label, e.attributes, e.scope, content: _*)
  def setAttribute(e: Elem, name: String, value: String) = new Elem(e.prefix, e.label, e.attributes.append(Attribute(None, name, Text(value), Null)), e.scope, e.child: _*)
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
        case e: Elem => replaceContent(e, transform(e.child, e +: parents))
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

    /**
     * Transforms each node found by this selector using the given function
     */
    def apply(fn: Node => NodeSeq): Unit = {
      addRule(selector, new ReplaceRule(fn))
    }

    def contents: RuleFactory = new RuleFactory(selector)  // TODO use child

    /**
     * Sets the contents of the matching elements to the given set of markup
     */
    def contents_=(nodes: NodeSeq): Unit = {
      def fn(): NodeSeq = nodes
      addRule(selector, ReplaceContentRule(fn))
    }

    /**
     * Sets the contents of the matching elements to the given text
     */
    def contents_=(text: String): Unit = {
      contents = Text(text)
    }

    /**
     * Sets the given attribute on each matching node found by this selector
     */
    def attribute(name: String, value: String): Unit = {
        def fn(node: Node) = value
        addRule(selector, SetAttributeRule(name, fn))
    }

    /**
     * Adds rules on the named attribute matching the current selections
     */
    def attribute(name: String) = new AttributeRuleFactory(name)


    class AttributeRuleFactory(name: String) {
      def value: RuleFactory = new RuleFactory(selector)  // TODO use attribute contents

      def value_=(text: String): Unit = {
        def fn(node: Node) = text
        addRule(selector, SetAttributeRule(name, fn))
      }

      def apply(fn: Node => String): Unit = {
        addRule(selector, SetAttributeRule(name, fn))
      }
    }
  }

  protected def addRule(selector: Selector, rule: Rule) = _rules(selector) = rule
}

/**
 * A helper class to make it easier to write new transformers within loops inside a parent transformer
 */
class Transform(nodes: NodeSeq) extends Transformer {
  implicit def toNodes(): NodeSeq = transform(nodes, Nil)
}