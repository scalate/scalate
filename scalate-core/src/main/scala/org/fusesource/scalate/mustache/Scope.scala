package org.fusesource.scalate.mustache

import org.fusesource.scalate.RenderContext
import collection.JavaConversions._
import java.{ lang => jl, util => ju }

object Scope {
  def apply(context: RenderContext) = RenderContextScope(context)
}

/**
 * Represents a variable scope
 *
 * @version $Revision : 1.1 $
 */
trait Scope {
  def parent: Option[Scope]

  def context: RenderContext

  /**
   * Renders the given variable name to the context
   */
  def renderVariable(name: String, unescape: Boolean): Unit = {
    val v = variable(name) match {
      case Some(v) => v
      case None =>
        parent match {
          case Some(p) => p.variable(name)
          case _ => null
        }
    }

    if (unescape) {
      context.unescape(v)
    }
    else {
      context.escape(v)
    }
  }

  /**
   * Returns the variable of the given name looking in this scope or parent scopes to resolve the variable
   */
  def variable(name: String): Option[_] = {
    val value = localVariable(name)
    value match {
      case Some(v) => value
      case _ => parent match {
        case Some(p) => p.variable(name)
        case _ => None
      }
    }
  }


  /**
   * Returns the variable in the local scope if it is defined
   */
  def localVariable(name: String): Option[_]

  def section(name: String)(block: Scope => Unit): Unit = {
    val value = variable(name)

    println("Evaluated value " + name + " = " + value + " in " + this)

    value match {
      case Some(v) =>
        toTraversable(v) match {
          case s: Traversable[Any] =>
            for (i <- s) {
              val scope = createScope(name, i)
              block(scope)
            }

          case true => block(this)
          case false =>
          case null =>

          case v => println("Don't understand value: " + v)
        }
      case None => parent match {
        case Some(ps) => ps.section(name)(block)
        case None => // do nothing, no value
      }
    }
  }

  def createScope(name: String, value: Any): Scope = {
    value match {
      case v: Map[String, Any] => new MapScope(this, name, v)
      case _ => new EmptyScope(this)
    //case u => throw new IllegalArgumentException("Cannot make Mustache Scope for value " + u)
    }
  }

  def toTraversable(v: Any): Any = v match {
    case f: Function0[_] => toTraversable(f())
    case c: ju.Collection[_] => asIterable(c)
    case i: ju.Iterator[_] => asIterator(i)
    case i: jl.Iterable[_] => asIterable(i)
    case _ => v
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