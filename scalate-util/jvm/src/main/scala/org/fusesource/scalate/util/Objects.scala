package org.fusesource.scalate.util

import slogging.LazyLogging

import java.lang.reflect.Constructor
import scala.reflect.ClassTag

/**
 * Helper object for working with objects using reflection
 */
object Objects extends LazyLogging {

  /**
   * A helper method to return a non null value or the default value if it is null
   */
  def getOrElse[T](value: T, defaultValue: => T) = if (value != null) value else defaultValue

  /**
   * Asserts that the given value is not null with a descriptive message
   */
  def notNull[T <: AnyRef](value: T, message: => String): T = {
    if (value == null) {
      throw new IllegalArgumentException(message)
    }
    value
  }

  def assertInjected[T <: AnyRef](value: T)(implicit m: ClassTag[T]): T = notNull(value, "Value of type " + m.runtimeClass.getName + " has not been injected!")

  /**
   * Instantiates the given object class using the possible list of values to be injected.
   *
   * Implements a really simple IoC mechanism. Ideally we'd improve this to support JSR330 style
   * better injection with annotated injection points or such like
   */
  def instantiate[T](clazz: Class[T], injectionValues: List[AnyRef] = Nil): T =
    tryInstantiate[T](clazz, injectionValues) match {
      case Some(v) => v
      case _ => throw new IllegalArgumentException("No valid constructor could be found for " + clazz.getName +
        " and values: " + injectionValues)
    }

  /**
   * Attempts to inject the given class if a constructor can be found
   */
  def tryInstantiate[T](clazz: Class[T], injectionValues: List[AnyRef] = Nil): Option[T] = {
    def argumentValue(paramType: Class[_]): Option[AnyRef] =
      injectionValues.find(paramType.isInstance(_))

    def create(c: Constructor[_], args: Array[AnyRef]): T = {
      val answer = if (args.isEmpty) {
        clazz.getConstructor().newInstance()
      } else {
        logger.debug("About to call constructor: %S on %s with args: %s", c, clazz.getName, args.toList)
        c.newInstance(args: _*)
      }
      answer.asInstanceOf[T]
    }

    def tryCreate(c: Constructor[_]): Option[T] = {
      val options = c.getParameterTypes.map(argumentValue(_))
      if (options.forall(_.isDefined)) {
        val args = options.map(_.get)
        Some(create(c, args))
      } else {
        None
      }
    }

    val constructors = clazz.getConstructors.sortBy(_.getParameterTypes.size * -1)
    constructors.view.map(c => tryCreate(c)).find(_.isDefined).flatten
  }
}
