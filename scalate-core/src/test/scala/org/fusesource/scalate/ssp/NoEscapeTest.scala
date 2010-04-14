package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
class NoEscapeTest extends TemplateTestSupport {

  test("disable markup escaping") {
    assertOutput("a = x > 5 && y < 3", """<% escapeMarkup = false %>
<% val foo = "x > 5 && y < 3" %>
a = ${foo}""")
  }

  test("using unescape function") {
    assertOutput( "b = x > 5 && y < 3", """<% val foo = "x > 5 && y < 3" %>
b = ${unescape(foo)}""")
  }

}