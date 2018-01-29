package org.fusesource.scalate.jade

class CaptureAppendAttributeTest extends JadeTestSupport {

  showOutput = true

  testRender(
    "capture append attributes",
    """
- captureAttributeAppend("foo")
  |hello
- captureAttributeAppend("foo")
  |world!
html
  head
    = attribute("foo")
""", """
<html>
  <head>
    hello
    world!

  </head>
</html>
""")
}