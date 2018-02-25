package org.fusesource.scalate.jade

class DynamicAttributeNameTest extends JadeTestSupport {

  showOutput = true

  testRender(
    "Use expressions inside attributes",
    """
html
  - val n = "foo"
  a{n => "bar"} Hey
""", """
<html>
  <a foo="bar">Hey</a>
</html>
""")
}