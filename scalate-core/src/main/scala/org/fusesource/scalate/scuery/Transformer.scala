/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.scuery

import _root_.org.fusesource.scalate.util.Logging
import _root_.org.fusesource.scalate.scuery.support._
import collection.mutable.{HashMap}
import xml.{Attribute, Document, Elem, Node, NodeSeq, Null, Text}
import XmlHelper._

/**
 * Allows simple XML replacement rules to be registered
 *
 * @version $Revision : 1.1 $
 */
class Transformer {
  protected val _rules = new HashMap[Selector, Rule]

  implicit def toSXml(node: Node) = SXml(node)

  implicit def toSXml(nodes: NodeSeq) = SXml(nodes)

  def $(cssSelector: String): RuleFactory = $(Selector(cssSelector))

  def $(selector: Selector): RuleFactory = new RuleFactory(selector)

  def apply(nodes: NodeSeq, ancestors: Seq[Node] = Nil): NodeSeq = {
    nodes.flatMap(transformNode(_, ancestors))
  }

  def apply(nodeAndAncestor: NodeAndAncestors): NodeSeq = apply(nodeAndAncestor.node, nodeAndAncestor.ancestors)

  /**
   * Transforms the given nodes passing in a block which is used to configure a new transformer
   * to transform the nodes. This method is typically used when performing nested transformations such
   * as transforming one or more nodes when inside a transformation rule itself.
   */
  def transform(nodes: NodeSeq, ancestors: Seq[Node])(rules: TransformerBuilder => Unit): NodeSeq = {
    val transformer = createChild
    rules(TransformerBuilder(transformer))
    transformer(nodes, ancestors)
  }

  def transform(nodes: NodeSeq)(rules: TransformerBuilder => Unit): NodeSeq = transform(nodes, Nil)(rules)

  def transform(nodes: NodeSeq, childTransformer: Transformer): NodeSeq = childTransformer.apply(nodes, Nil)


  /**
   * Creates a child transformer
   */
  def createChild: Transformer = {
    // TODO inherit transformer rules?
    val child = new Transformer()
    //child._rules ++= _rules
    child
  }

  protected def transformNode(node: Node, ancestors: Seq[Node]): NodeSeq = {
    val keys = _rules.filterKeys(_.matches(node, ancestors))
    val size = keys.size
    if (size == 0) {
      node match {
        case e: Elem => replaceContent(e, apply(e.child, e +: ancestors))
        case d: Document => apply(d.child)
        case n => n
      }
    }
    else {
      val rule = Rule(keys.valuesIterator)
      rule(node)
    }
  }

  class RuleFactory(selector: Selector) {

    /**
     * Transforms each node found by this selector using the given function
     */
    def apply(fn: Node => NodeSeq): Unit = {
      addRule(selector, new ReplaceRule(fn))
    }

    // Contents
    //-------------------------------------------------------------------------

    def contents = new ContentsRuleFactory()

    /**
     * Sets the contents of the matching elements to the given set of markup
     */
    def contents_=(nodes: NodeSeq): Unit = {
      def fn(node: Node): NodeSeq = nodes
      addRule(selector, ReplaceContentRule(fn))
    }

    /**
     * Sets the contents of the matching elements to the given text
     */
    def contents_=(text: String): Unit = {
      contents = Text(text)
    }

    /**
     * Sets the contents of the matching elements to the given number vlaue
     */
    def contents_=(number: Number): Unit = {
      val text = numberToText(number)
      contents = Text(text)
    }


    class ContentsRuleFactory {

      def apply(fn: Node => NodeSeq): Unit = {
        addRule(selector, ReplaceContentRule(fn))
      }

      def apply(nodes: NodeSeq): Unit = {
        update(nodes)
      }

      def apply(text: String): Unit = {
        update(text)
      }

      /**
       * Sets the contents of the matching elements to the given set of markup
       */
      def update(nodes: NodeSeq): Unit = {
        def fn(node: Node): NodeSeq = nodes
        addRule(selector, ReplaceContentRule(fn))
      }

      /**
       * Sets the contents of the matching elements to the given text
       */
      def update(text: String): Unit = {
        update(Text(text))
      }
    }

    // Attributes
    //-------------------------------------------------------------------------


    /**
     * Sets the given attribute on each matching node found by this selector
     */
    def attribute(name: String, value: String): AttributeRuleFactory = {
      attribute(name).value = value
    }

    /**
     * Adds rules on the named attribute matching the current selections
     */
    def attribute(name: String): AttributeRuleFactory = new AttributeRuleFactory(name)


    class AttributeRuleFactory(name: String) {
      def value: AttributeRuleFactory = this

      def value_=(text: String): AttributeRuleFactory = {
        def fn(node: Node) = text
        addRule(selector, SetAttributeRule(name, fn))
        this
      }

      def update(text: String): AttributeRuleFactory = {
        def fn(node: Node) = text
        addRule(selector, SetAttributeRule(name, fn))
        this
      }

      def apply(fn: Node => String): AttributeRuleFactory = {
        addRule(selector, SetAttributeRule(name, fn))
        this

      }

      def apply(text: => String): AttributeRuleFactory = {
        def fn(node: Node): String = text
        addRule(selector, SetAttributeRule(name, fn))
        this
      }
    }
  }

  protected def addRule(selector: Selector, rule: Rule) = {
    _rules(selector) = _rules.get(selector) match {
      case Some(r) => Rule(r, rule)
      case _ => rule
    }
  }

  /**
   * A strategy for converting numbers to text which may wish to use different formatters or Locales
   */
  protected def numberToText(number: Number) = number.toString
}

/**
 *  A helper class so that a function object can be used as a transformer
 */
case class TransformerBuilder(transformer: Transformer) {
  def apply(cssSelector: String) = transformer.$(cssSelector)

  def apply(selector: Selector) = transformer.$(selector)

}

/**
 * A helper class to pimp Scala's XML support to add easy SQuery filtering
 * so that you can perform a CSS3 selector on a [[scala.xml.Node]] or [[scala.xml.NodeSeq]]
 * via <code>xml.$("someSelector")</code>
 */
case class SXml(nodes: NodeSeq) {
  def $(cssSelector: String): NodeSeq = $(Selector(cssSelector))

  def $(selector: Selector): NodeSeq = selector.filter(nodes)
}

/**
 * Makes it easy to pass around a node object along with its ancestor
 */
case class NodeAndAncestors(node: Node, ancestors: Seq[Node]) {
  implicit def toNode = node
}