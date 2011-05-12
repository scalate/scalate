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

import collection.JavaConversions._
import java.{lang => jl, util => ju}
import xml.{NodeSeq, XML}
import org.fusesource.scalate.util.Log

object Scope extends Log {
  def apply(context: RenderContext) = {
    context.attributeOrElse[Scope]("scope", RenderContextScope(context))
  }
}

/**
 * Represents a variable scope
 *
 * @version $Revision : 1.1 $
 */
trait Scope {
  import Scope._
  def parent: Option[Scope]

  def context: RenderContext

  var implicitIterator: Option[String] = Some(".")

  /**
   * Renders the given variable name to the context
   */
  def renderVariable(name: String, unescape: Boolean): Unit = {
    val v = apply(name) match {
      case Some(a) => a
      case None =>
        parent match {
          case Some(p) => p.apply(name)
          case _ => null
        }
    }
    debug("renderVariable %s = %s on %s",name,v,this)
    renderValue(v, unescape)
  }

  def renderValue(v: Any, unescape: Boolean = false): Unit = if (unescape) {
    context.unescape(format(v))
  }
  else {
    context.escape(format(v))
  }


  /**
   * Returns the variable of the given name looking in this scope or parent scopes to resolve the variable
   */
  def apply(name: String): Option[Any] = {
    val value = localVariable(name)
    value match {
      case Some(v) => value
      case _ =>
        if (implicitIterator.isDefined && implicitIterator.get == name) {
          iteratorObject
        }
        else {
          parent match {
            case Some(p) => p.apply(name)
            case _ => None
          }
        }
    }
  }

  /**
   * Returns the current implicit iterator object
   */
  def iteratorObject: Option[Any] = None

  /**
   * Returns the variable in the local scope if it is defined
   */
  def localVariable(name: String): Option[Any]

  def section(name: String)(block: Scope => Unit): Unit = {
    apply(name) match {
      case Some(t) =>
        val v = toTraversable(t, block)
        debug("section value " + name + " = " + v + " in " + this)
        v match {

          // TODO we have to be really careful to distinguish between collections of things
          // such as Seq from objects / products / Maps / partial functions which act as something to lookup names

          case FunctionResult(r) => renderValue(r)

          case s: NodeSeq => childScope(name, s)(block)
          case s: Seq[Any] => foreachScope(name, s)(block)
          case Some(a) => childScope(name, a)(block)
          case None =>

          // lets treat empty maps as being empty collections
          // due to bug in JSON parser returning Map() for JSON expression []
          case s: collection.Map[_,_] => if (!s.isEmpty) childScope(name, s)(block)

          // maps and so forth, treat as child scopes
          case a: PartialFunction[_, _] => childScope(name, a)(block)

          // any other traversible treat as a collection
          case s: Traversable[Any] => foreachScope(name, s)(block)

          case true => block(this)
          case false =>
          case null =>

          // lets treat anything as an an object rather than a collection
          case a => childScope(name, a)(block)
        }
      case None => parent match {
        case Some(ps) => ps.section(name)(block)
        case None => // do nothing, no value
          debug("No value for " + name + " in " + this)

      }
    }
  }

  def invertedSection(name: String)(block: Scope => Unit): Unit = {
    apply(name) match {
      case Some(t) =>
        val v = toTraversable(t, block)
        debug("invertedSection value " + name + " = " + v + " in " + this)
        v match {

          // TODO we have to be really careful to distinguish between collections of things
          // such as Seq from objects / products / Maps / partial functions which act as something to lookup names

          case FunctionResult(r) =>

          case s: NodeSeq => if (s.isEmpty) block(this)
          case s: Seq[_] => if (s.isEmpty) block(this)
          case Some(a) =>
          case None => block(this)

          case s: collection.Map[_,_] => if (s.isEmpty) block(this)

          // maps and so forth, treat as child scopes
          case a: PartialFunction[_, _] =>

          // any other traversible treat as a collection
          case s: Traversable[Any] => if (s.isEmpty) block(this)

          case true =>
          case false => block(this)
          case null => block(this)

          // lets treat anything as an an object rather than a collection
          case a =>
        }
      case None => parent match {
        case Some(ps) => ps.invertedSection(name)(block)
        case None => block(this)
      }
    }
  }

  def partial(name: String): Unit = {
    context.withAttributes(Map("scope" -> this)) {
      // TODO allow the extension to be overloaded
      context.include(name + ".mustache")
    }
  }

  def childScope(name: String, v: Any)(block: Scope => Unit): Unit = {
    debug("Creating scope for: " + v)
    val scope = createScope(name, v)
    block(scope)
  }

  def foreachScope(name: String, s: Traversable[_])(block: Scope => Unit): Unit = {
    for (i <- s) {
      debug("Creating traversiable scope for: " + i)
      val scope = createScope(name, i)
      block(scope)
    }
  }

  def createScope(name: String, value: Any): Scope = {
    value match {
      case n: NodeSeq => new NodeScope(this, name, n)
      case v: collection.Map[String, _] => new MapScope(this, name, v)
      case null => new EmptyScope(this)
      case None => new EmptyScope(this)
      case v: AnyRef => new ObjectScope(this, v)
      case v =>
        warn("Unable to process value: %s", v)
        new EmptyScope(this)
    }
  }

  def toTraversable(v: Any, block: Scope => Unit): Any = v match {
    case t: Seq[_] => t
    case t: Array[_] => t.toSeq
    case t: ju.Map[_,_] => mapAsScalaMap(t)

    case f: Function0[_] => toTraversable(f(), block)
    case f: Function1[Scope, _] if isParam1(f, classOf[Scope]) => toTraversable(f(this), block)

    // lets call the function with the block as a text value
    case f: Function1[String, _] if isParam1(f, classOf[String]) =>
      FunctionResult(f(capture(block)))

    case c: ju.Collection[_] => collectionAsScalaIterable(c)
    case i: ju.Iterator[_] => asScalaIterator(i)
    case i: jl.Iterable[_] => iterableAsScalaIterable(i)

    case _ => v
  }

  def format(v: Any): Any = v match {
    case f: Function1[Scope, _] if isParam1(f, classOf[Scope]) => format(f(this))
    case _ => v
  }


  /**
   * Captures the output of the given block
   */
  def capture(block: Scope => Unit): String = {
    def body: Unit = block(this)
    context.capture(body)
  }

  def isParam1[T](f: Function1[T, _], clazz: Class[T]): Boolean = {
    try {
      f.getClass.getMethod("apply", clazz)
      true
    }
    catch {
      case e: NoSuchMethodException => false
    }
  }

}

/**
 * Scope for the render context
 */
case class RenderContextScope(context: RenderContext, defaultObjectName: Option[String] = Some("it")) extends Scope {
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
case class MarkupAttributeContextScope(context: RenderContext, attributeName: String) extends Scope {
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

class MapScope(parent: Scope, name: String, map: collection.Map[String, _]) extends ChildScope(parent) {
  def localVariable(name: String): Option[Any] = map.get(name)
}

class NodeScope(parent: Scope, name: String, node: NodeSeq) extends ChildScope(parent) {
  def localVariable(name: String): Option[Any] = Some(node \ name)
}

class EmptyScope(parent: Scope) extends ChildScope(parent) {
  def localVariable(name: String) = None
}

/**
 * Constructs a scope for a non-null and not None value
 */
class ObjectScope[T <: AnyRef](parent: Scope, value: T) extends ChildScope(parent) {
  val introspector = Introspector[T](value.getClass.asInstanceOf[Class[T]])

  def localVariable(name: String) = introspector.get(name, value)

  override def iteratorObject = Some(value)
}

case class FunctionResult(value: Any)