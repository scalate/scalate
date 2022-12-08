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
package org.fusesource.scalate.support

import org.fusesource.scalate.FunSuiteSupport
import java.{ util => ju }

// the following imports are included by default in TemplateEngine
import scala.jdk.CollectionConverters._
import TemplateConversions._

case class Address(city: String, country: String)
case class Person(name: String, address: Address)

class TemplateConversionsTest extends FunSuiteSupport {

  test("iterate over maps using Map.Entry like object") {
    val map = new ju.HashMap[String, String]
    map.put("a", "1")
    map.put("b", "2")

    for (e <- map.asScala) {
      val key = e.getKey
      val value = e.getValue
      log.info(" " + key + " = " + value)
    }
  }

  test("null pointer handling") {
    val a: String = null

    val answer = a ?: "default"
    log.info("got answer: " + answer)
    assertResult("default") { answer }
  }

  test("orElse method") {
    val a: String = null

    val answer = orElse(a, "default")
    log.info("got answer: " + answer)
    assertResult("default") { answer }
  }

  test("orElse with null pointer exception") {
    val person = Person("James", null)

    val answer = orElse(person.address.city, "default")
    log.info("got answer: " + answer)
    assertResult("default") { answer }
  }
}
