package org.fusesource.scalate.mustache

import org.fusesource.scalate.RenderContext
import collection.JavaConversions._
import _root_.java.{lang => jl, util => ju}
import org.fusesource.scalate.util.Logging

object Scope {
  def apply(context: RenderContext) = {
    context.attributeOrElse[Scope]("scope", RenderContextScope(context))
  }
}

/**
 * Represents a variable scope
 *
 * @version $Revision : 1.1 $
 */
trait Scope extends Logging {
  def parent: Option[Scope]

  def context: RenderContext

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
    debug("renderVariable " + name + " = " + v + " on " + this)
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
  def apply(name: String): Option[_] = {
    val value = localVariable(name)
    value match {
      case Some(v) => value
      case _ => parent match {
        case Some(p) => p.apply(name)
        case _ => None
      }
    }
  }


  /**
   * Returns the variable in the local scope if it is defined
   */
  def localVariable(name: String): Option[_]

  def section(name: String)(block: Scope => Unit): Unit = {
    apply(name) match {
      case Some(t) =>
        val v = toTraversable(t, block)
        debug("section value " + name + " = " + v + " in " + this)
        v match {

          // TODO we have to be really careful to distinguish between collections of things
          // such as Seq from objects / products / Maps / partial functions which act as something to lookup names

          case FunctionResult(r) => renderValue(r)

          case s: Seq[Any] => foreachScope(name, s)(block)

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

          case s: Seq[Any] => if (s.isEmpty) block(this)

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
      case v: Map[String, Any] => new MapScope(this, name, v)
      case null => new EmptyScope(this)
      case _ =>
        warn("Don't know how to create a scope for " + value)
        new EmptyScope(this)
    }
  }

  def toTraversable(v: Any, block: Scope => Unit): Any = v match {
    case t: Seq[_] => t
    case f: Function0[_] => toTraversable(f(), block)
    case f: Function1[Scope, _] if isParam1(f, classOf[Scope]) => toTraversable(f(this), block)

    // lets call the function with the block as a text value
    case f: Function1[String, _] if isParam1(f, classOf[String]) =>
      FunctionResult(f(capture(block)))

    case c: ju.Collection[_] => asIterable(c)
    case i: ju.Iterator[_] => asIterator(i)
    case i: jl.Iterable[_] => asIterable(i)
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

case class RenderContextScope(context: RenderContext) extends Scope {
  def parent: Option[Scope] = None

  def localVariable(name: String): Option[_] = context.attributes.get(name)
}

abstract class ChildScope(parentScope: Scope) extends Scope {
  def parent = Some(parentScope)

  def context = parentScope.context
}

class MapScope(parent: Scope, name: String, map: Map[String, _]) extends ChildScope(parent) {
  def localVariable(name: String): Option[_] = map.get(name)
}

class EmptyScope(parent: Scope) extends ChildScope(parent) {
  def localVariable(name: String) = None
}

case class FunctionResult(value: Any)