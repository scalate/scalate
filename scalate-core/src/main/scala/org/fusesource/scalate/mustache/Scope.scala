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
import org.fusesource.scalate.mustache.VariableResult.NoValue
import org.fusesource.scalate.mustache.VariableResult.NoVariable
import org.fusesource.scalate.mustache.VariableResult.SomeValue

import scala.jdk.CollectionConverters._

import java.{ lang => jl, util => ju }
import xml.NodeSeq
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
          case Some(p) if !name.contains(".") => p.apply(name)
          case _ => null
        }
    }
    debug("renderVariable %s = %s on %s", name, v, this)
    renderValue(v, unescape)
  }

  def renderValue(v: Any, unescape: Boolean = false): Unit = if (unescape) {
    context.unescape(format(v))
  } else {
    context.escape(format(v))
  }

  /**
   * Returns the variable of the given name looking in this scope or parent scopes to resolve the variable
   */
  def apply(name: String): Option[Any] = {
    dottedVariable(name, topLevel = true).toOption
  }

  private def variable(n: String, topLevel: Boolean) = {
    val value: VariableResult = localVariableOrImplicitIterator(n)
    if (topLevel) {
      value match {
        case NoVariable =>
          parent match {
            case Some(p) =>
              p.dottedVariable(n, true)
            case _ => NoVariable
          }
        case _ => value
      }
    } else {
      value
    }
  }

  private def localVariableOrImplicitIterator(name: String): VariableResult = {
    val value = VariableResult(localVariable(name), hasVariable(name))
    value match {
      case NoVariable =>
        if (implicitIterator.isDefined && implicitIterator.get == name) {
          VariableResult(iteratorObject, true)
        } else {
          NoVariable
        }
      case _ => value
    }
  }

  private def dottedVariable(name: String, topLevel: Boolean): VariableResult = {
    val headAndTail = splitByDot(name)
    headAndTail match {
      case None =>
        variable(name, topLevel)
      case Some((head, tail)) =>
        val headValue = variable(head, topLevel)
        headValue.flatMap {
          v =>
            val nestedScope = createScope(head, v)
            //ignore missing nested variables to keep Context Precedence
            nestedScope.dottedVariable(tail, topLevel = false).noVariableAsNoValue
        }
    }
  }

  private def splitByDot(name: String): Option[(String, String)] = {
    val dotPos = name.indexOf('.')
    if (dotPos <= 0) {
      None
    } else {
      val head = name.substring(0, dotPos)
      val tail = name.substring(dotPos + 1)
      Some((head, tail))
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

  def hasVariable(name: String): Boolean

  def section(name: String)(block: Scope => Unit): Unit = {
    val headAndTail = splitByDot(name)
    headAndTail match {
      case None =>
        sectionElementary(name)(block)
      case Some((head, tail)) =>
        sectionElementary(head)(_.section(tail)(block))
    }
  }

  private def sectionElementary(name: String)(block: Scope => Unit): Unit = {
    dottedVariable(name, topLevel = true) match {
      case SomeValue(t) =>
        val v = toIterable(t, block)
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
          case s: collection.Map[_, _] => if (!s.isEmpty) childScope(name, s)(block)

          // maps and so forth, treat as child scopes
          case a: PartialFunction[_, _] => childScope(name, a)(block)

          // any other traversable treat as a collection
          case s: Traversable[Any] => foreachScope(name, s.toIterable)(block)

          case true => block(this)
          case false =>
          case null =>

          // lets treat anything as an an object rather than a collection
          case a => childScope(name, a)(block)
        }
      case NoVariable => parent match {
        case Some(ps) => ps.section(name)(block)
        case None => // do nothing, no value
          debug("No value for " + name + " in " + this)
      }
      case NoValue =>
        // do nothing, no value
        debug("No value for " + name + " in " + this)
    }
  }

  def invertedSection(name: String)(block: Scope => Unit): Unit = {
    dottedVariable(name, topLevel = true) match {
      case SomeValue(t) =>
        val v = toIterable(t, block)
        debug("invertedSection value " + name + " = " + v + " in " + this)
        v match {

          // TODO we have to be really careful to distinguish between collections of things
          // such as Seq from objects / products / Maps / partial functions which act as something to lookup names

          case FunctionResult(r) =>

          case s: NodeSeq => if (s.isEmpty) block(this)
          case s: Seq[_] => if (s.isEmpty) block(this)
          case Some(a) =>
          case None => block(this)

          case s: collection.Map[_, _] => if (s.isEmpty) block(this)

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
      case _ => block(this)
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

  def foreachScope[T](name: String, s: Iterable[T])(block: Scope => Unit): Unit = {
    for (i <- s) {
      debug("Creating traversable scope for: " + i)
      val scope = createScope(name, i)
      block(scope)
    }
  }

  def createScope(name: String, value: Any): Scope = {
    value match {
      case n: NodeSeq => new NodeScope(this, name, n)
      case v: scala.collection.Map[_, _] =>
        new MapScope(
          this,
          name,
          v.asInstanceOf[scala.collection.Map[String, _]])
      case null => new EmptyScope(this)
      case None => new EmptyScope(this)
      case v: AnyRef => new ObjectScope(this, v)
      case v =>
        warn("Unable to process value: %s", v)
        new EmptyScope(this)
    }
  }

  @deprecated(message = "use toIterable instead", since = "")
  def toTraversable(v: Any, block: Scope => Unit): Any = toIterable(v, block)

  def toIterable(v: Any, block: Scope => Unit): Any = v match {
    case t: Seq[_] => t
    case t: Array[_] => t.toSeq
    case t: ju.Map[_, _] => t.asScala

    case f: Function0[_] => toIterable(f(), block)
    case f: Function1[_, _] =>
      if (isParam1(f, classOf[Object])) {
        // Java lambda support since 1.8
        try {
          val f2 = f.asInstanceOf[Function1[Scope, _]]
          toIterable(f2(this), block)
        } catch {
          case e: Exception =>
            try {
              val f2 = f.asInstanceOf[Function1[String, _]]
              FunctionResult(f2(capture(block)))
            } catch {
              case e: Exception =>
                f
            }
        }
      } else {
        f
      }

    case c: ju.Collection[_] => c.asScala
    case i: ju.Iterator[_] => i.asScala
    case i: jl.Iterable[_] => i.asScala

    case _ => v
  }

  def format(v: Any): Any = v match {
    case f: Function0[_] => format(f())
    case f: Function1[_, _] if isParam1(f, classOf[Scope]) => format(f.asInstanceOf[Function1[Scope, _]](this))
    case f: Function1[_, _] =>
      try {
        format(f.asInstanceOf[Function1[Object, _]](this))
      } catch {
        case e: ClassCastException =>
          v
      }
    case _ => v
  }

  /**
   * Captures the output of the given block
   */
  def capture(block: Scope => Unit): String = {
    def body(): Unit = block(this)
    context.capture(body())
  }

  def isParam1[T](f: Function1[_, _], clazz: Class[T]): Boolean = {
    try {
      f.getClass.getMethod("apply", clazz)
      true
    } catch { case e: NoSuchMethodException => false }
  }

}
