package org.fusesource.scalate.ssp

import org.fusesource.scalate.{TemplateSource, TemplateTestSupport}

/**
 * @version $Revision : 1.1 $
 */
class LayoutTest extends TemplateTestSupport {

  test("layout with no param") {
    val source = TemplateSource.fromText("sample.ssp", """
#do( layout("layout.ssp") )
 location: <b>London</b>
#end
""")
    assertOutputContains(source, "<body>", "location:", "London", "</body>")
  }

  test("layout with empty Map") {
    val source = TemplateSource.fromText("sample3.ssp", """
<% layout("layout.ssp", Map()) { %>
 location: <b>London</b>
<% } %>
""")
    assertOutputContains(source, "<body>", "location:", "London", "</body>")
  }

  test("layout with Map") {
    val source = TemplateSource.fromText("sample4.ssp", """
<% layout("layout.ssp", Map("foo" -> 1)) { %>
 location: <b>London</b>
<% } %>
""")
    assertOutputContains(source, "<body>", "location:", "London", "</body>")
  }

}