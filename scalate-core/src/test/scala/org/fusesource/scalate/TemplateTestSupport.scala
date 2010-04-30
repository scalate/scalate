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
    debug("output: '" + output + "'")

    if (trim) {
      output = output.trim
    }
    expect(expectedOutput) {output}
  }

  def assertOutputContains(source: TemplateSource, expected: String*): Unit = assertOutputContains(source, Map[String, Any](), expected: _*)

  def assertOutputContains(source: TemplateSource, attributes: Map[String, Any], expected: String*): Unit = {
    val template = engine.compile(source)
    assertOutputContains(template, attributes, expected: _*)
  }

  def assertOutputContains(template: Template, expected: String*): Unit = assertOutputContains(template, Map[String, Any](), expected: _*)

  def assertOutputContains(template: Template, attributes: Map[String, Any], expected: String*): Unit = {
    var output = engine.layout(template, attributes)
    debug("output: '" + output + "'")

    assertTextContains(output, "template " + template, expected: _*)
  }

  def assertTextContains(source: String, description: => String, textLines: String*): Unit = {
    assume(source != null, "text was null for " + description)
    var index = 0
    for (text <- textLines if index >= 0) {
      index = source.indexOf(text, index)
      if (index >= 0) {
        index += text.length
      }
      else {
        assume(false, "Text does not contain '" + text + "' for " + description + " when text was:\n" + source)
      }
    }
  }

  def syntaxException(block: => Unit) = {
    val e = intercept[InvalidSyntaxException] {
      block
    }
    debug("caught: " + e, e)
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