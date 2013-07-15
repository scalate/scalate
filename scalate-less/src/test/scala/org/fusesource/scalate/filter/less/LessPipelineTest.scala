package org.fusesource.scalate.filter.less

import org.fusesource.scalate.test.TemplateTestSupport
import org.fusesource.scalate.support.TemplateFinder

class LessPipelineTest extends TemplateTestSupport {

  lazy val finder = new TemplateFinder(engine)

  override protected def fromUri(uri: String) = {
    val newUri = finder.findTemplate(uri).getOrElse(uri)
    super.fromUri(newUri)
  }

  test("standalone") {
    assertUriOutputContains("org/fusesource/scalate/filter/less/standalone.css", """p {
  text-color: blue;
}
""")

  }
}