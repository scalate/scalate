package org.fusesource.scalate.scuery

import xml.{Attribute, Elem, Node, NodeSeq}

/**
 * A transformer which makes it easy to create nested transformations by switching the thread local
 * transformer used for the '$' methods when inside of a transform method.
 *
 * @version $Revision : 1.1 $
 */
class NestedTransformer {
  private val rootTransformer = new Transformer
  private val threadLocal = new ThreadLocal[Transformer] {
    override def initialValue = rootTransformer
  }

  def $(cssSelector: String) = transformer.$(cssSelector)

  def $(selector: Selector) = transformer.$(selector)

  def apply(nodes: NodeSeq, parents: Seq[Node] = Nil) = transformer(nodes, parents)

  def transformer: Transformer = threadLocal.get


  /**
   * Transforms the given nodes passing in a block which is used to configure a new transformer
   * to transform the nodes. This method is typically used when performing nested transformations such
   * as transforming one or more nodes when inside a transformation rule itself.
   */
  def transform(nodes: NodeSeq, parents: Seq[Node])(rules: Transformer => Unit): NodeSeq = {
    val currentTransform = transformer
    try {
      val childTransform = transformer.createChild
      threadLocal.set(childTransform)
      rules(childTransform)
      childTransform(nodes, parents)
    }
    catch {
      case e => threadLocal.set(currentTransform)
      throw e

    }
  }

  def transform(nodes: NodeSeq)(rules: Transformer => Unit): NodeSeq = transform(nodes, Nil)(rules)

}