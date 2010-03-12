package org.fusesource.scalate.introspector

import java.beans.{PropertyDescriptor, Introspector => BeanInt}
import java.lang.reflect.Method
import org.fusesource.scalate.util.ProductReflector

object Introspector {
  def apply(aType: Class[_]) = {
    if (classOf[Product].isAssignableFrom(aType)) {
      new ProductIntrospector(aType)
    }
    else {
      new BeanIntrospector(aType)
    }
  }
}

case class Property(name: String, propertyType: Class[_], label: String)

/**
 * @version $Revision: 1.1 $
 */
trait Introspector {
  def elementType: Class[_]

  /**
   * Returns the CSS style name of the introspection type
   */
  def typeStyleName: String =  BeanInt.decapitalize(elementType.getSimpleName)

  def properties:Seq[Property]
}

class BeanIntrospector(val elementType: Class[_]) extends Introspector {
  val beanInfo = BeanInt.getBeanInfo(elementType)
  val _properties = beanInfo.getPropertyDescriptors.filter(p => p.getReadMethod != null && p.getName != "class").map(createProperty(_))

  def properties = {
    _properties
  }

  protected def createProperty(descriptor: PropertyDescriptor) = {
    val label = descriptor.getName
    // TODO look for an annotation on the model - or a viewModel?
    new Property(descriptor.getName, descriptor.getPropertyType, label)
  }
}

class ProductIntrospector(val elementType: Class[_]) extends Introspector {
  def properties = ProductReflector.accessorMethods(elementType).map(createProperty(_))

  protected def createProperty(method: Method) = {
    val label = method.getName
    // TODO look for an annotation on the model - or a viewModel?
    new Property(method.getName, method.getReturnType, label)
  }

}