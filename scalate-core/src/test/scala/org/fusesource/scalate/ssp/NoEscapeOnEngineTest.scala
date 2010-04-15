package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
class NoEscapeOnEngineTest extends TemplateTestSupport {
  engine.escapeMarkup = false

  test("markup escaping disabled") {
    assertSspOutput("a = x > 5 && y < 3", """<% val foo = "x > 5 && y < 3" %>
a = ${foo}""")
  }
}