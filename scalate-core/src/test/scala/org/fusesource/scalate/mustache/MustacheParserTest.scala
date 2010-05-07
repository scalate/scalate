package org.fusesource.scalate.mustache

import org.fusesource.scalate.{InvalidSyntaxException, FunSuiteSupport}

/**
 * @version $Revision : 1.1 $
 */

class MustacheParserTest extends FunSuiteSupport {

  implicit def stringToText(x: String) = Text(x)

  
  test("plain text") {
    assertValid("some text more text")

    assertParses(List(Text("some text more text")), """some text more text""")
  }

  test("variable") {
    assertParses(List(Text("some text "), Variable("foo"), Text(" "), Variable("bar"), Text(" more text")),
      "some text {{foo}} {{bar}} more text")
  }

  test("unescape variable") {
    assertParses(List(Text("some text "), Variable("foo", true), Text(" "), Variable("bar", true), Text(" more text")),
      "some text {{&foo}} {{& bar}} more text")
  }

  test("unescape with treble moustache") {
    assertParses(List(Text("some text "), Variable("foo", true), Text(" more text")),
      "some text {{{foo}}} more text")
  }

  test("open close tag") {
    assertParses(List( Text("* "), Section("foo", List(Text(" bar "))), Text(" *")),
      "* {{#foo}} bar {{/foo}} *")
  }

  test("invert variable and partial") {
    assertParses(List( Text("* "), InvertSection("foo", List(Text(" "), Partial("bar"), Text(" "))), Text(" *")),
      "* {{^foo}} {{>bar}} {{/foo}} *")
  }

  // set delimiter
  test("just set directive") {
    assertParses(List(SetDelimiter("<%", "%>")),
      "{{=<% %>=}}")
  }

  test("text and set directive") {
    assertParses(List( Text("* "), SetDelimiter("<%", "%>"), Text(" *")),
      "* {{=<% %>=}} *")
  }

  test("set directive") {
    assertParses(List(Text("* "), Variable("default_tags"), Text(" * "),
      SetDelimiter("<%", "%>"), Text(" * "), Variable("erb_style_tags"), Text(" * "),
      SetDelimiter("{{", "}}"), Text(" * "), Variable("default_tags_again")),
      "* {{default_tags}} * {{=<% %>=}} * <% erb_style_tags %> * <%={{ }}=%> * {{ default_tags_again }}")
  }


  // test bad syntax
  assertFail("text {{-}}")
  assertFail("text {{")
  assertFail("text {{}")
  assertFail("text {{}}")

  test("missing end tag") {
    expectSyntaxException("Missing end tag '{{/foo}}' for started tag at 1.3") {
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
      println("=> " + line)
    }
    debug("")
    lines
  }

  def assertParses(expected: List[Statement], text: String): List[Statement] = {
    val lines = assertValid(text)
    expect(expected) {lines}
    lines
  }

  def syntaxException(block: => Unit) = {
    val e = intercept[InvalidSyntaxException] {
      block
    }
    debug("caught: " + e, e)
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
    assert(e.getMessage.contains(message), "InvalidSyntaxException message did not contain the text: \n  "+message+"\nInstead got: \n  "+e.getMessage)
  }
}
