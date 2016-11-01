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
package org.fusesource.scalate.mustache

import org.fusesource.scalate.{ InvalidSyntaxException, FunSuiteSupport }
import org.fusesource.scalate.util.IOUtil
import java.io.File

/**
 * @version $Revision : 1.1 $
 */

class MustacheParserTest extends FunSuiteSupport {

  implicit def stringToText(x: String) = Text(x)

  test("set directive") {
    assertParses(
      List(Text("* "), Variable("default_tags"), Text(" * "),
        SetDelimiter("<%", "%>"), Text("* "), Variable("erb_style_tags"), Text(" * "),
        SetDelimiter("{{", "}}"), Text("* "), Variable("default_tags_again")),
      "* {{default_tags}} * {{=<% %>=}} * <% erb_style_tags %> * <%={{ }}=%> * {{ default_tags_again }}"
    )
  }

  test("plain text") {
    assertValid("some text more text")

    assertParses(List(Text("some text more text")), """some text more text""")
  }

  test("variable") {
    assertParses(
      List(Text("some text "), Variable("foo"), Text(" "), Variable("bar"), Text(" more text")),
      "some text {{foo}} {{bar}} more text"
    )
  }

  test("unescape variable") {
    assertParses(
      List(Text("some text "), Variable("foo", true), Text(" "), Variable("bar", true), Text(" more text")),
      "some text {{&foo}} {{& bar}} more text"
    )
  }

  test("unescape with treble moustache") {
    assertParses(
      List(Text("some text "), Variable("foo", true), Text(" more text")),
      "some text {{{foo}}} more text"
    )
  }

  test("open close section") {
    assertParses(
      List(Text("* "), Section("foo", List(Text("bar "))), Text("*")),
      "* {{#foo}} bar {{/foo}} *"
    )
  }

  test("invert variable and partial") {
    assertParses(
      List(Text("* "), InvertSection("foo", List(Partial("bar"))), Text("*")),
      "* {{^foo}} {{>bar}}{{/foo}} *"
    )
  }

  // set delimiter
  test("just set directive") {
    assertParses(
      List(SetDelimiter("<%", "%>")),
      "{{=<% %>=}}"
    )
  }

  test("text and set directive") {
    assertParses(
      List(Text("* "), SetDelimiter("<%", "%>"), Text("*")),
      "* {{=<% %>=}} *"
    )
  }

  test("whitespace with sections") {
    assertParses(
      List(
        Section("terms", List(Variable("name"), Text("\n  "),
          Variable("index"), Text("\n"))),
        Section("terms", List(Variable("name"), Text("\n  "), Variable("index"), Text("\n")))
      ),
      loadTestFile("reuse_of_enumerables.html")
    )
  }

  test("newline after expression") {
    assertParses(
      List(Variable("greeting"), Text(", "), Variable("name"), Text("!")),
      loadTestFile("two_in_a_row.html")
    )
  }

  ignore("complex whitespace") {
    assertParses(
      List(Variable("greeting"), Text(", "), Variable("name"), Text("!")),
      loadTestFile("complex.html")
    )
  }

  // test bad syntax
  assertFail("text {{-}}")
  assertFail("text {{")
  assertFail("text {{}")
  assertFail("text {{}}")

  test("missing end tag") {
    expectSyntaxException("Missing section end '{{/foo}}' for section beginning at 1.3") {
      "* {{#foo}} bar "
    }
  }

  def assertValid(text: String): List[Statement] = {
    debug("Parsing...")
    debug(text)
    debug("")

    val lines = (new MustacheParser).parse(text)
    for (line <- lines) {
      debug("=> " + line)
    }
    debug("")
    lines
  }

  def assertParses(expected: List[Statement], text: String): List[Statement] = {
    val lines = assertValid(text)
    expect(expected) { lines }
    lines
  }

  def syntaxException(block: => Unit) = {
    val e = intercept[InvalidSyntaxException] {
      block
    }
    debug(e, "caught: " + e)
    e
  }

  def assertFail(template: String): Unit = {
    test("bad syntax: " + template) {
      syntaxException {
        assertValid(template)
      }
    }
  }

  def expectSyntaxException(message: String)(block: => String): Unit = {
    val e = intercept[InvalidSyntaxException] {
      assertValid(block)
    }
    assert(e.getMessage.contains(message), "InvalidSyntaxException message did not contain the text: \n  " + message + "\nInstead got: \n  " + e.getMessage)
  }

  protected def loadTestFile(name: String) = IOUtil.loadTextFile(new File(baseDir, "src/test/resources/moustache/js/" + name))
}
