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
package org.fusesource.scalate
package util

import org.scalatest.matchers.should.Matchers

object LogTest

class LogTest extends FunSuiteSupport with Matchers {
  case class InnerTrait()

  case object InnerObject

  test("logger postfix for top-level class is delimited with '.'") {
    val log = Log(classOf[LogTest], "postfix")
    log.log.getName should equal("org.fusesource.scalate.util.LogTest.postfix")
  }

  test("logger postfix for inner class is delimited with '#'") {
    val log = Log(classOf[InnerTrait], "postfix")
    log.log.getName should equal("org.fusesource.scalate.util.LogTest#InnerTrait.postfix")
  }

  test("Trailing '#' is trimmed from class name before applying postfix") {
    val log = Log(LogTest.getClass, "postfix")
    log.log.getName should equal("org.fusesource.scalate.util.LogTest.postfix")
  }

  test("Trailing '#' is trimmed from inner class name before applying postfix") {
    val log = Log(InnerObject.getClass, "postfix")
    log.log.getName should equal("org.fusesource.scalate.util.LogTest#InnerObject.postfix")
  }
}
