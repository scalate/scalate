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
package org.fusesource.scalate.converter

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.FunSuite
import _root_.java.io.File
import _root_.org.fusesource.scalate._

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ExpressionParseTest extends FunSuite {

  assertConvert("${foo.bar}", "foo.getBar")
  assertConvert("${foo[123]}", "foo(123)")
  assertConvert("${foo.bar[123]}", "foo.getBar(123)")
  assertConvert("${foo.bar lt 5}", "foo.getBar < 5")
  assertConvert("${foo.bar eq 5}", "foo.getBar == 5")
  assertConvert("${it.language eq 'foo'}", "it.getLanguage == \"foo\"")

  assertConvert("${empty foo.bar}", "foo.getBar isEmpty")
  assertConvert("${x && empty foo.bar}", "x && foo.getBar isEmpty")

  assertConvert("${not empty foo.bar}", "!(foo.getBar isEmpty)")
  assertConvert("${x && not empty foo.bar}", "x && !(foo.getBar isEmpty)")

  assertConvert("${fn:length(foo.bar)}", "foo.getBar.size")

  def assertConvert(el: String, ssp: String): String = {
    val result = convert(el)
    expect(ssp, "converting EL: " + el) { result }
    result
  }

  def convert(el: String): String = {
    println("Converting EL: " + el)

    val parser = new ExpressionParser
    val exp = parser.parseExpression(el)
    println("Expression: " + exp)

    val result = exp.asParam

    println(" => " + result)
    println
    result
  }
}
