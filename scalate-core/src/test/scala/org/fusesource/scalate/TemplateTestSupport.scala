/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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


import java.io.File

abstract class TemplateTestSupport extends FunSuiteSupport {
  val engine = new TemplateEngine
  engine.workingDirectory = new File("target/test-data/" + getClass.getSimpleName)
  var printOutput = false
  var printExceptions = true

  def compileSsp(text: String) = engine.compileSsp(text)
  def compileScaml(text: String) = engine.compileScaml(text)

  def assertTrimSspOutput(expectedOutput: String, templateText: String, attributes: Map[String, Any] = Map()): Unit = assertSspOutput(expectedOutput, templateText, attributes, true)

  def assertTrimOutput(expectedOutput: String, template: Template, attributes: Map[String, Any] = Map()): Unit = assertOutput(expectedOutput, template, attributes, true)

  def assertSspOutput(expectedOutput: String, templateText: String, attributes: Map[String, Any] = Map(), trim: Boolean = false): Unit = {
    val template = engine.compileSsp(templateText)

    assertOutput(expectedOutput, template, attributes, trim)
  }

  def assertOutput(expectedOutput: String, template: Template, attributes: Map[String, Any] = Map(), trim: Boolean = false): Unit = {
    var output = engine.layout(template, attributes)
    if (printOutput) {
      println("output: '" + output + "'")
    }

    if (trim) {
      output = output.trim
    }
    expect(expectedOutput) {output}
  }

  def syntaxException(block: => Unit) = {
    val e = intercept[InvalidSyntaxException] {
      block
    }
    if (printExceptions) {
      println("caught: " + e)
    }
    e
  }

  def testSspSyntaxEception(name: String, template: String): Unit = {
    test(name) {
      syntaxException {
        assertSspOutput("xxx", template)
      }
    }
  }
}