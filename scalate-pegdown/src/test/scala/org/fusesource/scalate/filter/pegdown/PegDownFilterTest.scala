package org.fusesource.scalate.filter.pegdown

import org.fusesource.scalate.test.TemplateTestSupport

class PegDownFilterTest extends TemplateTestSupport {

  test("template with multimarkdown filter") {
    showOutput = true

    assertUriOutputContains("sample.jade", "Welcome to <strong>Scalate</strong> I hope you like it!")
  }

}
