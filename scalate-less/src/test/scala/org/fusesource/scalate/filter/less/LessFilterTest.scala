package org.fusesource.scalate.filter.less

import org.fusesource.scalate.test.TemplateTestSupport

class LessFilterTest extends TemplateTestSupport {
  test("inline") {
    assertUriOutputContains("/org/fusesource/scalate/filter/less/inline.scaml", """
<style type="text/css">
p {
  text-color: #0000ff;
}
</style>
""".trim)
  }

  test("inline include") {
    assertUriOutputContains("/org/fusesource/scalate/filter/less/inline_include.scaml", """
<style type="text/css">
div.section {
  border: solid 1px blue;
  border-radius: 5px;
  -moz-border-radius: 5px;
  -webkit-border-radius: 5px;
}
</style>
""".trim)
  }
}
