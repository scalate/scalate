package org.fusesource.scalate.mustache

import org.fusesource.scalate.RenderContext

/**
 * Represents a variable scope
 * 
 * @version $Revision : 1.1 $
 */
trait Scope {
  def parent: Option[Scope] = None

  /**
   * Renders the given variable name to the context
   */
  def renderVariable(context: RenderContext, name: String, unescape: Boolean): Unit = {
    val v = evaluateVariable(context, name) match {
      case Some(v) => v
      case None =>
        parent match {
          case Some(p) => p.evaluateVariable(context, name)
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


  def evaluateVariable(context: RenderContext, name: String): Option[_]
}

object RenderContextScope extends Scope {

  def evaluateVariable(context: RenderContext, name: String): Option[_] = context.attributes.get(name)

}