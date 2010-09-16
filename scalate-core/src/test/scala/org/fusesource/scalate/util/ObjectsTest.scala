/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package org.fusesource.scalate
package util

import Objects._
import scalate.Boot

class ObjectsTest extends FunSuiteSupport {
  test("inject no params") {
    val a = assertInstantiate(classOf[NoParams])
    expect("Hello") {a.value}
  }

  // single values
  testInject(StringParam("James"), classOf[StringParam], List("James"))

  // derived values
  test("inject derived class") {
    assertInstantiate(classOf[Boot], List(new TemplateEngine()))
  }

  protected def assertInstantiate[T](clazz: Class[T], injectValues: List[AnyRef] = List()): T = {
    val answer = instantiate(clazz, injectValues)
    assert(answer != null, "Should have instantiated an instance of " + clazz.getName)
    debug("Instantiated: " + answer)
    answer

  }

  protected def testInject(expected: AnyRef, clazz: Class[_], injectValues: List[AnyRef] = List()): Unit = {
    test("inject " + clazz.getName + " with " + injectValues) {
      expect(expected) {assertInstantiate(clazz, injectValues)}
    }
  }
}

case class StringParam(name: String)

class NoParams {
  val value = "Hello"
}

