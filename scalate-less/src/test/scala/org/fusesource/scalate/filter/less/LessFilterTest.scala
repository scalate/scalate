package org.fusesource.scalate.filter.less

import org.fusesource.scalate.test.TemplateTestSupport

class LessFilterTest extends TemplateTestSupport {
  test("inline") {
    assertUriOutputContains("/org/fusesource/scalate/filter/less/inline.scaml", """
<style type="text/css">
p {
  text-color: blue;
}
</style>
""".trim)
  }
}