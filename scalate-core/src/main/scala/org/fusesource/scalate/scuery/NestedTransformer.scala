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

  def apply(nodes: NodeSeq, ancestors: Seq[Node] = Nil) = transformer(nodes, ancestors)

  def transformer: Transformer = threadLocal.get


  /**
   * Transforms the given nodes passing in a block which is used to configure a new transformer
   * to transform the nodes. This method is typically used when performing nested transformations such
   * as transforming one or more nodes when inside a transformation rule itself.
   */
  def transform(nodes: NodeSeq, ancestors: Seq[Node])(rules: Transformer => Unit): NodeSeq = {
    val currentTransform = transformer
    try {
      val childTransform = transformer.createChild
      threadLocal.set(childTransform)
      rules(childTransform)
      childTransform(nodes, ancestors)
    }
    catch {
      case e => threadLocal.set(currentTransform)
      throw e

    }
  }

  def transform(nodes: NodeSeq)(rules: Transformer => Unit): NodeSeq = transform(nodes, Nil)(rules)

}