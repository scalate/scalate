package org.fusesource.scalate.scuery

import xml.{Attribute, Document, Elem, Node, NodeSeq, Null, Text}

object Transform {
  implicit def toNodes(transform: Transform): NodeSeq = transform()
  implicit def toTraversable(transform: Transform): Traversable[Node] = transform()
}

/**
 * A helper class to make it easier to write new transformers within loops inside a ancestor transformer
 */
class Transform(val nodes: NodeSeq, ancestors: Seq[Node] = Nil) extends Transformer {
  def apply(): NodeSeq = apply(nodes, ancestors)

  implicit def toNodes(): NodeSeq = apply()

  implicit def toTraversable(): Traversable[Node] = apply()
}
