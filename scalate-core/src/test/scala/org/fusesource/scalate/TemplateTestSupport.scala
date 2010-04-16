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
import java.lang.String
import collection.immutable.Map

abstract class TemplateTestSupport extends FunSuiteSupport {
  val engine = new TemplateEngine
  
  var printOutput = false
  var printExceptions = true

  override protected def beforeAll(configMap: Map[String, Any]) = {
    super.beforeAll(configMap)

    engine.workingDirectory = new File(baseDir, "target/test-data/" + getClass.getSimpleName)
  }

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


  protected def safeName(text: String): String =
    text.foldLeft(new StringBuffer)((acc, ch) => safeName(ch, acc)).toString

  private def safeName(ch: Char, buffer: StringBuffer): StringBuffer = {
    if (ch == '&') {
      buffer.append("amp_")
    }
    else if (ch == '>') {
      buffer.append("gt_")
    }
    else if (ch == '<') {
      buffer.append("lt_")
    }
    else if (ch == '=') {
      buffer.append("eq_")
    }
    else if (ch == '!') {
      buffer.append("pling_")
    }
    else if (ch == '/') {
      buffer.append("/")
    }
    else if (Character.isDigit(ch) || Character.isJavaIdentifierPart(ch) || ch == '_' || ch == '.') {
      buffer.append(ch)
    }
    else {
      buffer.append('_')
    }
    buffer
  }


  def compileScaml(name: String, templateText: String) = engine.compile(TemplateSource.fromText(safeName(name) + ".scaml", templateText))

  def compileSsp(name: String, templateText: String) = engine.compile(TemplateSource.fromText(safeName(name) + ".ssp", templateText))
}