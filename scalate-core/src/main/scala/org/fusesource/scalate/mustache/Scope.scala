package org.fusesource.scalate.mustache

import org.fusesource.scalate.RenderContext
import java.lang.String

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
    val v = evaluateVariable(name) match {
      case Some(v) => v
      case None =>
        parent match {
          case Some(p) => p.evaluateVariable(name)
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


  def evaluateVariable(name: String): Option[_]

  def section(name: String)(block: Scope => Unit): Unit = {
    val value = evaluateVariable(name)

    println("Evaluated value " + name + " = " + value + " in " + this)

    value match {
      case Some(v) =>
        v match {
          case s: Seq[Any] =>
            for (i <- s) {
              val scope = createScope(name, i)
              block(scope)
            }
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

}

case class RenderContextScope(context: RenderContext) extends Scope {
  def parent: Option[Scope] = None
  def evaluateVariable(name: String): Option[_] = context.attributes.get(name)
}

abstract class ChildScope(parentScope: Scope) extends Scope {
  def parent = Some(parentScope)
  def context = parentScope.context
}

class MapScope(parent: Scope, name: String, map: Map[String, _]) extends ChildScope(parent) {
  def evaluateVariable(name: String): Option[_] = map.get(name)
}

class EmptyScope(parent: Scope) extends ChildScope(parent) {
  def evaluateVariable(name: String) = None
}