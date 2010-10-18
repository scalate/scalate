package org.fusesource.scalate
package ssp

class AndyWEscapeTest extends TemplateTestSupport {

  test("escape text") {
    assertOutputContains(fromUri("org/fusesource/scalate/ssp/escape.ssp"),
     """<div id="a">foo=&quot;bar baz&quot; &lt;this&gt;</div>""" ,
     """<div id="b">foo=&quot;bar baz&quot; &lt;this&gt;</div>""" ,
     """<div id="c">foo=&quot;bar baz&quot; &lt;this&gt;</div>"""
    )
  }

}