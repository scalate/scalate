package org.fusesource.scalate.support

import org.fusesource.scalate.FunSuiteSupport
import java.{util => ju}

// the following imports are included by default in TemplateEngine
import scala.collection.JavaConversions._
import TemplateConversions._

case class Address(city: String, country: String)
case class Person(name: String, address: Address)

class TemplateConversionsTest extends FunSuiteSupport {

  test("iterate over maps using Map.Entry like object") {
    val map = new ju.HashMap[String,String]
    map.put("a", "1")
    map.put("b", "2")

    for (e <- map) {
      val key = e.getKey
      val value = e.getValue
      println(" " + key + " = " + value)
    }
  }

  test("null pointer handling") {
    val a: String = null

    val answer = a ?: "default"
    println("got answer: " + answer)
    expect("default"){answer}
  }

  test("orElse method") {
    val a: String = null

    val answer = orElse(a, "default")
    println("got answer: " + answer)
    expect("default"){answer}
  }

  test("orElse with null pointer exception") {
    val person = Person("James", null)

    val answer = orElse(person.address.city, "default")
    println("got answer: " + answer)
    expect("default"){answer}
  }
}