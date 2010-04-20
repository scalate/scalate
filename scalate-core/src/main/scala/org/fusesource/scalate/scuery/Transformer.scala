package org.fusesource.scalate.scuery

import _root_.org.fusesource.scalate.util.Logging
import _root_.org.fusesource.scalate.scuery.support._
import collection.mutable.{HashMap, ListBuffer}
import xml.{Attribute, Document, Elem, Node, NodeSeq, Null, Text}


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

  def apply(nodes: NodeSeq, parents: Seq[Node] = Nil): NodeSeq = {
    nodes.flatMap(transformNode(_, parents))
  }

  /**
   * Transforms the given nodes passing in a block which is used to configure a new transformer
   * to transform the nodes. This method is typically used when performing nested transformations such
   * as transforming one or more nodes when inside a transformation rule itself.
   */
  def transform(nodes: NodeSeq, parents: Seq[Node])(rules: TransformerBuilder => Unit): NodeSeq = {
    val transformer = createChild
    rules(TransformerBuilder(transformer))
    transformer(nodes, parents)
  }

  def transform(nodes: NodeSeq)(rules: TransformerBuilder => Unit): NodeSeq = transform(nodes, Nil)(rules)


  /**
   * Creates a child transformer
   */
  def createChild: Transformer = {
    // TODO inherit transformer rules?
    val child = new Transformer()
    //child._rules ++= _rules
    child
  }

  protected def transformNode(node: Node, parents: Seq[Node]): NodeSeq = {
    val keys = _rules.filterKeys(_.matches(node, parents))
    val size = keys.size
    if (size == 0) {
      node match {
        case e: Elem => replaceContent(e, apply(e.child, e +: parents))
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

/*
    def contents(fn: Node => NodeSeq): Unit = {
      addRule(selector, ReplaceContentRule(fn))
    }

    def contents(nodes: NodeSeq): Unit = {
      contents.update(nodes)
    }

    def contents(text: String): Unit = {
      contents.update(text)
    }
*/

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
}

/**
 * A helper class so that a function object can be used as a transformer
 */
case class TransformerBuilder(transformer: Transformer) {
  def apply(cssSelector: String) = transformer.$(cssSelector)

  def apply(selector: Selector) = transformer.$(selector)

}