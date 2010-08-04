package org.fusesource.scalate.util

import org.fusesource.scalate.FunSuiteSupport
import java.{util => ju}

// the following imports are included by default in TemplateEngine
import scala.collection.JavaConversions._
import org.fusesource.scalate.util.TemplateConversions._

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
}