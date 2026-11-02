package org.fusesource.scalate.jade

/**
 * Checks that a value written with `=` on the same line as textarea or pre is
 * not indented after newlines. Previously the indentation was inserted, so the
 * second and following lines of a textarea's initial value started with spaces.
 */
class JadeWhitespacePreserveTest extends JadeTestSupport {

  testRender(
    "textarea value is not indented",
    """
div
  div
    textarea= "a\nb\n\nc"
    p= "a\nb"
""",
    """
<div>
  <div>
    <textarea>a
b

c</textarea>
    <p>a
    b</p>
  </div>
</div>
"""
  )

  testRender(
    "pre value is not indented",
    """
div
  pre= "a\nb"
""",
    """
<div>
  <pre>a
b</pre>
</div>
"""
  )

  testRender(
    "textarea value is still escaped",
    """
div
  textarea= "</textarea>\n<b>&"
""",
    """
<div>
  <textarea>&lt;/textarea&gt;
&lt;b&gt;&amp;</textarea>
</div>
"""
  )
}
