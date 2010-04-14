package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
class NoEscapeOnEngineTest extends FunSuiteSupport {
  val engine = new TemplateEngine
  engine.escapeMarkup = false
  engine.workingDirectory = new File(baseDir, "target/test-data/NoEscapeOnEngineTest")

  test("markup escaping disabled") {
    assertOutput("a = x > 5 && y < 3", """<% val foo = "x > 5 && y < 3" %>
a = ${foo}""")
  }

  def assertOutput(expectedOutput: String, templateText: String): Unit = {
    val template = engine.compileSsp(templateText)

    val output = engine.layout(template)
    println("output: '" + output + "'")

    expect(expectedOutput) { output }
  }
}