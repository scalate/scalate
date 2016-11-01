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
package org.fusesource.scalate.util

import java.lang.reflect.Constructor
import scala.reflect.ClassTag

/**
 * Helper object for working with objects using reflection
 */
object Objects {
  val log = Log(getClass); import log._

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

    def create(c: Constructor[_], args: Array[AnyRef] = Array()): T = {
      val answer = if (args.isEmpty) {
        clazz.newInstance
      } else {
        debug("About to call constructor: %S on %s with args: %s", c, clazz.getName, args.toList)
        c.newInstance(args: _*)
      }
      answer.asInstanceOf[T]
    }

    def tryCreate(c: Constructor[_]): Option[T] = {
      val options = c.getParameterTypes.map(argumentValue(_))
      if (options.forall(_.isDefined)) {
        val args = options.map(_.get).toArray
        Some(create(c, args))
      } else {
        None
      }
    }

    val constructors = clazz.getConstructors.sortBy(_.getParameterTypes.size * -1)
    constructors.view.map(c => tryCreate(c)).find(_.isDefined).getOrElse(None)
  }
}