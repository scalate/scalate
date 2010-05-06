package org.fusesource.scalate.scuery

import xml.{Node, NodeSeq}

/**
 * All the various implicit conversions for the scuery package
 */
trait ScueryConversions {
  implicit def toNodes(transform: Transform): NodeSeq = transform()

  implicit def toSXml(node: Node) = SXml(node)

  implicit def toSXml(nodes: NodeSeq) = SXml(nodes)

  implicit def toNode(nodeAndAncestors: NodeAndAncestors): Node = nodeAndAncestors.node
}
