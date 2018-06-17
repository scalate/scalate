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

import _root_.org.fusesource.scalate.FunSuiteSupport

case class MyProduct(name: String, age: Int) {
  // should be a value which returns a function
  def bold(text: String) = "<b>" + text + "</b>"
}

class MyBean {
  var _name: String = _

  def getName() = _name

  def setName(name: String): Unit = _name = name

  var _age: Int = _

  def getAge() = _age

  def setAge(age: Int): Unit = _age = age

  // not a bean property but visible anyway
  def foo = "bar"

  // should be a value which returns a function
  def bold(text: String) = "<b>" + text + "</b>"

  override def toString = "MyBean(" + getName + ", " + getAge + ")"
}

class IntrospectorTest extends FunSuiteSupport {

  test("product introspector") {
    val introspector = Introspector(classOf[MyProduct])
    assertResult("myProduct") { introspector.typeStyleName }

    val properties = introspector.properties.sortBy(_.name)
    assertProperties(properties, 2)

    assertProperty(properties(0), "age", "age", classOf[Int])
    assertProperty(properties(1), "name", "name", classOf[String])
  }

  test("bean introspector") {
    val introspector = Introspector(classOf[MyBean])
    assertResult("myBean") { introspector.typeStyleName }

    val properties = introspector.properties.sortBy(_.name)
    assertProperties(properties, 2)

    assertProperty(properties(0), "age", "age", classOf[Int])
    assertProperty(properties(1), "name", "name", classOf[String])
  }

  test("product get") {
    val v = MyProduct("James", 40)
    val introspector = Introspector(classOf[MyProduct])
    dump(introspector)

    assertResult(Some("James")) { introspector.get("name", v) }
    assertResult(Some(40)) { introspector.get("age", v) }

    assertStringFunctor(introspector, v, "bold", "product", "<b>product</b>")
  }

  test("bean get") {
    val v = new MyBean
    v.setName("Hiram")
    v.setAge(30)

    val introspector = Introspector(classOf[MyBean])
    dump(introspector)

    assertResult(Some("Hiram")) { introspector.get("name", v) }
    assertResult(Some(30)) { introspector.get("age", v) }

    // autodiscover methods too
    assertResult(Some("bar")) { introspector.get("foo", v) }

    assertStringFunctor(introspector, v, "bold", "bean", "<b>bean</b>")
  }

  test("bean set") {
    val v = new MyBean

    val introspector = Introspector(classOf[MyBean])
    val name = introspector.property("name").get
    val age = introspector.property("age").get

    name.set(v, "James")
    assertResult("James") { name(v) }

    age.set(v, 30)
    assertResult(30) { age(v) }

    debug("created bean: %s", v)
    // TODO....
  }

  def dump[T](introspector: Introspector[T]): Unit = {
    debug("Introspector for %s", introspector.elementType.getName)
    val expressions = introspector.expressions
    for (k <- expressions.keysIterator.toSeq.sortWith(_ < _)) {
      debug("Expression: %s = %s", k, expressions(k))
    }
  }

  def assertStringFunctor[T](introspector: Introspector[T], instance: T, name: String, arg: String, expected: Any): Unit = {
    introspector.get(name, instance) match {
      case Some(f: Function1[_, _]) =>
        val _f = f.asInstanceOf[Function1[String, _]]
        debug("calling function %s named %s on %s = %s", _f, name, instance, _f(arg))
        assertResult(expected) { _f(arg) }
      case Some(v) =>
        fail("Expected function for expression " + name + " but got " + v)
      case _ =>
        fail("Expected function for expression " + name)
    }
  }

  def assertProperty(property: Property[_], name: String, label: String, propertyType: Class[_]) = {
    assertResult(name) { property.name }
    assertResult(label) { property.label }
    assertResult(propertyType) { property.propertyType }
  }

  def assertProperties(properties: collection.Seq[Property[_]], expectedSize: Int) = {
    for (property <- properties) {
      debug("Property: %s", property)
    }
    assertResult(expectedSize) { properties.size }
  }
}
