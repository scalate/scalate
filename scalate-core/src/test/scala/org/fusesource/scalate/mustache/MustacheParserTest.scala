package org.fusesource.scalate.mustache

import org.fusesource.scalate.{InvalidSyntaxException, FunSuiteSupport}

/**
 * @version $Revision : 1.1 $
 */

class MustacheParserTest extends FunSuiteSupport {
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

  test("open close tag") {
    assertParses(List( Text("* "), TagOpen("foo"), Text(" bar "), TagClose("foo"), Text(" *")),
      "* {{#foo}} bar {{/foo}} *")
  }

  test("invert variable and partial") {
    assertParses(List( Text("* "), InvertVariable("foo"), Text(" "), Partial("bar"), Text(" *")),
      "* {{^foo}} {{>bar} *")
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
      SetDelimiter("<%", "%>"), Text(" * "), Variable("erb_style_tags"), Text(" *"),
      SetDelimiter("{{", "}}"), Text(" * "), Variable("default_tags_again")),
      "* {{default_tags}} * {{=<% %>=}} * <% erb_style_tags %> * <%={{ }}=%> * {{ default_tags_again }}")
  }


  // test bad syntax
  test("bad expressions") {
    badExpression("text {{-}}")
    badExpression("text {{")
    badExpression("text {{}")
    badExpression("text {{}}")
  }

  implicit def stringToText(x: String) = Text(x)

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

  /**
   * lets assume bad expressions just render text instead
   */
  def badExpression(template: String): Unit = {
    assertParses(List(Text(template)), template)  
  }

}
