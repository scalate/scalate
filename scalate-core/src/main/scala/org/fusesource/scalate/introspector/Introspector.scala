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
package org.fusesource.scalate.introspector

import org.fusesource.scalate.util.ProductReflector

import java.beans.{ PropertyDescriptor, Introspector => BeanInt }
import java.util.concurrent.locks.ReentrantReadWriteLock
import collection.mutable.{ HashMap, Map, WeakHashMap }
import java.lang.reflect.{ Method, Modifier }

object Introspector {

  private[this] val rwl = new ReentrantReadWriteLock()
  private[this] val rlock = rwl.readLock
  private[this] val wlock = rwl.writeLock

  /**
   * The global caching strategy for introspection which by default uses a weak hash map
   * to avoid keeping around cached data for classes which are garbage collected
   */
  private[this] val cache = WeakHashMap.empty[Class[_], Introspector[_]]

  /**
   * Returns the Introspector for the given type using the current cache if it is defined
   */
  def apply[T](aType: Class[T]): Introspector[T] = {
    safeGetOrElseUpdate(aType).asInstanceOf[Introspector[T]]
  }

  /**
   * Does thread-safe access and modification of the cache map using read and write locks
   */
  private def safeGetOrElseUpdate[T](key: Class[T]): Introspector[_] = {
    def get(): Option[Introspector[_]] = {
      rlock.lock
      try {
        cache.get(key).filterNot(_ == null)
      } finally rlock.unlock
    }
    def update(): Introspector[_] = {
      wlock.lock
      try {
        get() getOrElse {
          cache.remove(key)
          val d = createIntrospector(key)
          cache.put(key, d)
          d
        }
      } finally wlock.unlock
    }
    get().getOrElse(update())
  }

  /**
   * Creates a new introspector
   */
  def createIntrospector(aType: Class[_]): Introspector[_] = {
    if (classOf[Product].isAssignableFrom(aType)) {
      new ProductIntrospector(aType)
    } else {
      new BeanIntrospector(aType)
    }
  }
}

/**
 * @version $Revision : 1.1 $
 */
trait Introspector[T] {
  private[this] lazy val _expressions = createExpressions

  def elementType: Class[T]

  /**
   * Returns the CSS style name of the introspection type
   */
  def typeStyleName: String = decapitalize(elementType.getSimpleName)

  def properties: collection.Seq[Property[T]]

  lazy val propertyMap = Map[String, Property[T]](properties.map(p => p.name -> p).toSeq: _*)

  def property(name: String): Option[Property[T]] = propertyMap.get(name) match {
    case s: Some[Property[T]] => s
    case _ =>
      // lets allow bad case to find the property if it finds exactly one match
      val found = properties.filter(_.name.equalsIgnoreCase(name))
      if (found.size == 1) {
        Some(found.head)
      } else {
        None
      }
  }

  /**
   * Returns the value by name on the given instance.
   *
   * Typically this method returns the named property or returns the method with no parameters.
   * If no property or method with zero arguments is available then a suitable function is searched for
   * such as a function which takes a String parameter if using higher order functions or tags on objects.
   */
  def get(name: String, instance: T): Option[Any] = {
    expressions.get(name) match {
      case Some(e: Expression[T]) =>
        Some(e.evaluate(instance))
      case _ => None
    }
  }

  /**
   * Returns all the expressions available for the given type
   */
  def expressions: Map[String, Expression[T]] = _expressions

  protected def createExpressions: Map[String, Expression[T]] = {
    val answer = new HashMap[String, Expression[T]]
    for (p <- properties) {
      answer += p.name -> p
    }

    /**
     * Lazily create and add the given property using the method name
     * and the bean property style name of the method if it has not already
     * been added
     */
    def add(method: Method, property: => Property[T]): Unit = {
      val name = method.getName
      answer.getOrElseUpdate(name, property)
      if (name.matches("get\\p{javaUpperCase}.*")) {
        val propertyName = decapitalize(name.substring(3))
        answer.getOrElseUpdate(propertyName, property)
      }
    }

    val nonVoidPublicMethods = elementType.getMethods.filter(m => Modifier.isPublic(m.getModifiers) && isValidReturnType(m.getReturnType))

    // include all methods which have no arguments
    for (m <- nonVoidPublicMethods if m.getParameterTypes.isEmpty) {
      add(m, new MethodProperty(m))
    }

    // include all methods which have a single String parameter
    for (m <- nonVoidPublicMethods if isSingleParameterOfType(m, classOf[String])) {
      add(m, new StringFunctorProperty(m))
    }
    answer
  }

  protected def isSingleParameterOfType(method: Method, paramType: Class[_]): Boolean = {
    val types = method.getParameterTypes
    types.size == 1 && paramType.isAssignableFrom(types(0))
  }

  protected def decapitalize(name: String): String = BeanInt.decapitalize(name)

  protected def isValidReturnType(clazz: Class[_]): Boolean = clazz != classOf[Void] && clazz != Void.TYPE
}

trait Expression[T] {
  def evaluate(instance: T): Any
}

trait Property[T] extends Expression[T] {
  def name: String

  def propertyType: Class[_]

  def label: String

  def description: String

  def readOnly: Boolean

  def optional: Boolean

  def apply(instance: T): Any = evaluate(instance)

  def evaluate(instance: T): Any

  def set(instance: T, value: Any): Unit
}

class BeanIntrospector[T](val elementType: Class[T]) extends Introspector[T] {
  val beanInfo = BeanInt.getBeanInfo(elementType)
  val _properties = beanInfo.getPropertyDescriptors.filter(p => p.getReadMethod != null && p.getName != "class").map(createProperty(_))

  def properties = {
    _properties
  }

  protected def createProperty(descriptor: PropertyDescriptor) = new BeanProperty[T](descriptor)
}

/**
 * A property for a Java Bean property
 */
case class BeanProperty[T](descriptor: PropertyDescriptor) extends Property[T] {
  def name = descriptor.getName

  def propertyType = descriptor.getPropertyType

  def readOnly = descriptor.getWriteMethod == null

  def optional = descriptor.getWriteMethod != null && !propertyType.isPrimitive

  // TODO use annotations to find description / label?
  def label = descriptor.getDisplayName

  def description = descriptor.getShortDescription

  def evaluate(instance: T) = descriptor.getReadMethod.invoke(instance)

  def set(instance: T, value: Any) = descriptor.getWriteMethod.invoke(instance, value.asInstanceOf[AnyRef])

  override def toString = "BeanProperty(" + name + ": " + propertyType.getName + ")"
}

class ProductIntrospector[T](val elementType: Class[T]) extends Introspector[T] {
  def properties = ProductReflector.accessorMethods(elementType).map(createProperty(_))

  protected def createProperty(method: Method) = new MethodProperty[T](method)
}

/**
 * A property which just maps to a method with no arguments
 */
class MethodProperty[T](method: Method) extends Property[T] {
  def name = method.getName

  def propertyType = method.getReturnType

  def readOnly = true

  def optional = false

  def label = name

  def description = name

  def evaluate(instance: T) = method.invoke(instance)

  def set(instance: T, value: Any) = throw new UnsupportedOperationException("Cannot set " + this)

  override def toString = "MethodProperty(" + name + ": " + propertyType.getName + ")"
}

/**
 * A property which returns a Function which when invoked it invokes the underlying
 * method on the given object
 */
class StringFunctorProperty[T](method: Method) extends MethodProperty[T](method) {
  override def evaluate(instance: T) = {
    def f(arg: String) = method.invoke(instance, arg)
    f _
  }

  override def toString = "StringFunctorProperty(" + name + ")"
}
