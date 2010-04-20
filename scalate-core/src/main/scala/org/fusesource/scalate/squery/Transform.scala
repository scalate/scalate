package org.fusesource.scalate.squery

import xml.{Attribute, Document, Elem, Node, NodeSeq, Null, Text}

object Transform {
  implicit def toNodes(transform: Transform): NodeSeq = transform()
}

/**
 * A helper class to make it easier to write new transformers within loops inside a parent transformer
 */
class Transform(val nodes: NodeSeq, parents: Seq[Node] = Nil) extends Transformer {
  def apply(): NodeSeq = apply(nodes, parents)

  implicit def toNodes(): NodeSeq = apply()

}
