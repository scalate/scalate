package org.fusesource.scalate.introspector

import java.beans.{PropertyDescriptor, Introspector => BeanInt}
import org.fusesource.scalate.util.ProductReflector
import collection.mutable.{HashMap, Map, WeakHashMap}
import java.lang.reflect.{Modifier, Method}

object Introspector {

  /**
   * The global caching strategy for introspection which by default uses a weak hash map
   * to avoid keeping around cached data for classes which are garbage collected
   */
  var cache: Option[Map[Class[_], Introspector]] = Some(new WeakHashMap[Class[_], Introspector])

  /**
   * Returns the Introspector for the given type using the current cache if it is defined
   */
  def apply(aType: Class[_]): Introspector = {
    cache match {
      case Some(m) => m.getOrElseUpdate(aType, createIntrospector(aType))
      case _ => createIntrospector(aType)
    }
  }

  /**
   * Creates a new introspector
   */
  def createIntrospector(aType: Class[_]): Introspector = {
    if (classOf[Product].isAssignableFrom(aType)) {
      new ProductIntrospector(aType)
    }
    else {
      new BeanIntrospector(aType)
    }
  }
}

/**
 * @version $Revision : 1.1 $
 */
trait Introspector {
  private lazy val _expressions = createExpressions

  def elementType: Class[_]

  /**
   * Returns the CSS style name of the introspection type
   */
  def typeStyleName: String = decapitalize(elementType.getSimpleName)

  def properties: Seq[Property[_]]

  /**
   * Returns the value by name on the given instance.
   *
   * Typically this method returns the named property or returns the method with no parameters.
   * If no property or method with zero arguments is available then a suitable function is searched for
   * such as a function which takes a String parameter if using higher order functions or tags on objects.
   */
  def get(name: String, instance: Any): Option[_] = {
    expressions.get(name) match {
      case Some(e: Expression[_]) => Some(e.evaluate(instance))
      case _ => None
    }
  }

  /**
   * Returns all the expressions available for the given type
   */
  def expressions: Map[String, Expression[_]] = _expressions

  protected def createExpressions: Map[String, Expression[_]] = {
    val answer = new HashMap[String, Expression[_]]
    for (p <- properties) {
      answer += p.name -> p
    }

    /**
     * Lazily create and add the given property using the method name
     * and the bean property style name of the method if it has not already
     * been added
     */
    def add(method: Method, property: => Property[_]): Unit = {
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

  def set(instance: T, value: AnyRef): Unit
}

class BeanIntrospector(val elementType: Class[_]) extends Introspector {
  val beanInfo = BeanInt.getBeanInfo(elementType)
  val _properties = beanInfo.getPropertyDescriptors.filter(p => p.getReadMethod != null && p.getName != "class").map(createProperty(_))

  def properties = {
    _properties
  }

  protected def createProperty(descriptor: PropertyDescriptor) = new BeanProperty(descriptor)
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

  def set(instance: T, value: AnyRef) = descriptor.getWriteMethod.invoke(instance, value)

  override def toString = "BeanProperty(" + name + ": " + propertyType.getName + ")"
}

class ProductIntrospector(val elementType: Class[_]) extends Introspector {
  def properties = ProductReflector.accessorMethods(elementType).map(createProperty(_))

  protected def createProperty(method: Method) = new MethodProperty(method)
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

  def set(instance: T, value: AnyRef) = throw new UnsupportedOperationException("Cannot set " + this)

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
