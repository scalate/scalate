package org.fusesource.scalate.filter

import org.fusesource.scalate.TemplateTestSupport
import org.fusesource.scalate.util.ResourceLoader
import org.fusesource.scalate.support.TemplateFinder

class CoffeeScriptPipelineTest extends TemplateTestSupport {

  lazy val finder = new TemplateFinder(engine)

  test("coffeescript pipeline") {
    assertUriOutputContains(
      "/org/fusesource/scalate/filter/sample.js",
      """alert("Hello, Coffee!");
"""
    )
  }

  override protected def fromUri(uri: String) = {
    val newUri = finder.findTemplate(uri).getOrElse(uri)
    super.fromUri(newUri)
  }
}