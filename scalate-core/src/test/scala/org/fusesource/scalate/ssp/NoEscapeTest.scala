package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
class NoEscapeTest extends FunSuiteSupport {
  val engine = new TemplateEngine
  engine.workingDirectory = new File(baseDir, "target/test-data/NoEscapeTest")

  test("disable markup escaping") {
    assertOutput("a = x > 5 && y < 3", """<% escapeMarkup = false %>
<% val foo = "x > 5 && y < 3" %>
a = ${foo}""")
  }

  test("using unescape function") {
    assertOutput( "b = x > 5 && y < 3", """<% val foo = "x > 5 && y < 3" %>
b = ${unescape(foo)}""")
  }

  def assertOutput(expectedOutput: String, templateText: String): Unit = {
    val template = engine.compileSsp(templateText)

    val output = engine.layout(template)
    println("output: '" + output + "'")

    expect(expectedOutput) { output }
  }
}