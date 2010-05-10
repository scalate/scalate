package org.fusesource.scalate.scuery

import xml.{Attribute, Elem, NodeSeq, Null, Text}

/**
 * @version $Revision: 1.1 $
 */
object XmlHelper extends ScueryConversions {
  def replaceContent(e: Elem, content: NodeSeq) = new Elem(e.prefix, e.label, e.attributes, e.scope, content: _*)

  def setAttribute(e: Elem, name: String, value: String) = new Elem(e.prefix, e.label, e.attributes.append(Attribute(None, name, Text(value), Null)), e.scope, e.child: _*)
}