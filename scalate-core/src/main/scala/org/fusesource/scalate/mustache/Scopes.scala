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
package org.fusesource.scalate.mustache

import org.fusesource.scalate.RenderContext
import org.fusesource.scalate.introspector.Introspector

import xml.{ NodeSeq, XML }

/**
 * Scope for the render context
 */
case class RenderContextScope(
  context: RenderContext,
  defaultObjectName: Option[String] = Some("it")) extends Scope {

  // lets create a parent scope which is the defaultObject scope so we look there last
  val rootScope = MarkupAttributeContextScope(context, "html")

  val _parent: Option[Scope] = defaultObjectName match {
    case Some(name) => apply(name) match {
      case Some(value) => Some(rootScope.createScope(name, value))
      case _ => Some(rootScope)
    }
    case _ => Some(rootScope)
  }

  def parent: Option[Scope] = _parent

  def localVariable(name: String): Option[Any] = context.attributes.get(name)
}

/**
 * A context intended for use in layouts which looks up an attribute in the render context and if it exists
 * returns a new child scope for walking the templates markup
 */
case class MarkupAttributeContextScope(
  context: RenderContext,
  attributeName: String) extends Scope {

  def parent = None

  def localVariable(name: String) = if (name == attributeName) {
    // lets get the context from the attributes
    // by default this is stored in the 'body' attribute in the current
    // layout mechanism
    context.attributes.get("body") match {
      case Some(t) =>
        val text = t.toString
        // lets create a markup scope
        Some(XML.loadString(text))

      case v =>
        None
    }
  } else None
}

abstract class ChildScope(parentScope: Scope) extends Scope {

  implicitIterator = parentScope.implicitIterator

  def parent = Some(parentScope)

  def context = parentScope.context
}

class MapScope(
  parent: Scope,
  name: String,
  map: collection.Map[String, _]) extends ChildScope(parent) {

  def localVariable(name: String): Option[Any] = map.get(name)
}

class NodeScope(
  parent: Scope,
  name: String,
  node: NodeSeq) extends ChildScope(parent) {

  def localVariable(name: String): Option[Any] = Some(node \ name)
}

class EmptyScope(
  parent: Scope) extends ChildScope(parent) {

  def localVariable(name: String) = None
}

/**
 * Constructs a scope for a non-null and not None value
 */
class ObjectScope[T <: AnyRef](
  parent: Scope,
  value: T) extends ChildScope(parent) {

  val introspector = Introspector[T](value.getClass.asInstanceOf[Class[T]])

  def localVariable(name: String) = introspector.get(name, value)

  override def iteratorObject = Some(value)
}

case class FunctionResult(
  value: Any)
