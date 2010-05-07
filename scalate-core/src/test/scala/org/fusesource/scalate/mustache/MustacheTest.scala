package org.fusesource.scalate.mustache

import org.fusesource.scalate.TemplateTestSupport

/**
 * @version $Revision: 1.1 $
 */
class MustacheTest extends TemplateTestSupport {

  test("plain text") {
    assertMoustacheOutput("Hey James!", "Hey James!", Map("name" -> "Foo"))
  }

  test("simple variables") {
    assertMoustacheOutput("Hey James!", "Hey {{name}}!", Map("name" -> "James"))
  }
}