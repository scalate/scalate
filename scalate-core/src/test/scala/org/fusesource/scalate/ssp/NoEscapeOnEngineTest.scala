package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
class NoEscapeOnEngineTest extends TemplateTestSupport {
  test("markup escaping disabled") {
    assertSspOutput("a = x > 5 && y < 3", """<% val foo = "x > 5 && y < 3" %>
a = ${foo}""")
  }

  override protected def createTemplateEngine = {
    val engine = super.createTemplateEngine
    engine.escapeMarkup = false
    engine
  }
}