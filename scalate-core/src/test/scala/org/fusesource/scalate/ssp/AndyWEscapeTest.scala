package org.fusesource.scalate
package ssp

class AndyWEscapeTest extends TemplateTestSupport {

  test("escape text") {
    showOutput = true

    assertOutputContains(fromUri("org/fusesource/scalate/ssp/escape.ssp"),
     """<div id="a">foo=&quot;bar baz&quot; &lt;this&gt;</div>"""
    )
  }

}