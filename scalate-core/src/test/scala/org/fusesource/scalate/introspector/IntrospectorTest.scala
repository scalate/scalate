package org.fusesource.scalate.introspector

import java.io.File
import _root_.org.fusesource.scalate.Asserts._
import _root_.org.fusesource.scalate.FunSuiteSupport
import _root_.org.fusesource.scalate.util.Logging

case class MyProduct(name: String, age: Int) {}

class MyBean {
  var _name: String = _
  def getName() = _name
  def setName(name: String): Unit = _name = name

  var _age: Int = _
  def getAge() = _age
  def setAge(age: Int): Unit = _age = age
}

class IntrospectorTest extends FunSuiteSupport {


  test("product introspector") {
    val introspector = Introspector(classOf[MyProduct])
    expect("myProduct") { introspector.typeStyleName }

    val properties = introspector.properties.sortBy(_.name)
    assertProperties(properties, 2)

    assertProperty(properties(0), "age", "age", classOf[Int])
    assertProperty(properties(1), "name", "name", classOf[String])
  }


  test("bean introspector") {
    val introspector = Introspector(classOf[MyBean])
    expect("myBean") { introspector.typeStyleName }

    val properties = introspector.properties.sortBy(_.name)
    assertProperties(properties, 2)

    assertProperty(properties(0), "age", "age", classOf[Int])
    assertProperty(properties(1), "name", "name", classOf[String])
  }


  def assertProperty(property: Property, name: String, label: String, propertyType: Class[_]) = {
    expect(name) {property.name}
    expect(label) {property.label}
    expect(propertyType) {property.propertyType}
  }

  def assertProperties(properties: Seq[Property], expectedSize: Int) = {
    for (property <- properties) {
      debug("Property: " + property)
    }
    expect(expectedSize) { properties.size }
  }
}