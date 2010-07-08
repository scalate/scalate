package org.fusesource.scalate.ssp

import org.fusesource.scalate.TemplateTestSupport

/**
 * @version $Revision : 1.1 $
 */
class MultipleTypeParameterTest extends TemplateTestSupport {

  test("Zeus issue with multiple type parameters") {

    assertTrimSspOutput("map is (foo,bar)", """
<%@ val aMap: Map[String, String]  = Map("foo" -> "bar") %>
map is ${aMap}
""")
  }
}