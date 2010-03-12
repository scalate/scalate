package org.fusesource.scalate.introspector

/**
 * @version $Revision: 1.1 $
 */


import org.fusesource.scalate.Asserts
import org.fusesource.scalate.util.Logging
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import java.io.File

case class MyProduct(name: String, age: Int) {}

class MyBean {
  var _name: String = _
  def getName() = _name
  def setName(name: String): Unit = _name = name

  var _age: Int = _
  def getAge() = _age
  def setAge(age: Int): Unit = _age = age


}

@RunWith(classOf[JUnitRunner])
class IntrospectorTest extends FunSuite with Logging {


  test("product introspector") {
    val introspector = Introspector(classOf[MyProduct])
    expect("myProduct") { introspector.typeStyleName }

    val properties = introspector.properties
    assertProperties(properties, 2)

    assertProperty(properties(0), "name", "name", classOf[String])
    assertProperty(properties(1), "age", "age", classOf[Int])
  }


  test("bean introspector") {
    val introspector = Introspector(classOf[MyBean])
    expect("myBean") { introspector.typeStyleName }

    val properties = introspector.properties
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
      println("Property: " + property)
    }
    expect(expectedSize) { properties.size }
  }
}