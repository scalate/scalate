package org.fusesource.scalate
package support

import util.Objects

import scala.language.reflectiveCalls

/**
 * A helper class for working with Boot style classes which
 * take a TemplateEngine as a constructor argument and have a zero argument
 * run method.
 */
object Boots {

  def invokeBoot(clazz: Class[_], injectionParameters: List[AnyRef]): Unit = {
    // Structural Typing to make Reflection easier.
    type Boot = {
      def run: Unit
    }

    lazy val bootClassName = clazz.getName
    val o = try {
      Objects.instantiate(clazz, injectionParameters).asInstanceOf[Boot]
    } catch {
      case e: VirtualMachineError => throw e
      case e: ThreadDeath => throw e
      case e: Throwable => throw new TemplateException("Failed to create the instance of class " + bootClassName, e)
    }

    try {
      o.run
    } catch {
      case e: VirtualMachineError => throw e
      case e: ThreadDeath => throw e
      case e: Throwable => throw new TemplateException("Failed to invoke " + bootClassName + ".run() : " + e, e)
    }
  }
}
