package org.fusesource.scalate.jruby

import org.fusesource.scalate.test.TemplateTestSupport

class SassTest extends TemplateTestSupport {

  test("sass template") {
    showOutput = true

    assertUriOutputContains("sample.jade", "<style", "color: #2b9eab;", "</style>")
  }
}