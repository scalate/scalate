package org.fusesource.scalate.jade

import org.fusesource.scalate.TemplateSource

/**
 * Code written with `-` must be indented consistently with the rest of the generated code.
 * It used to be indented twice, which made Scala 3 warn "Line is indented too far to the left".
 */
class JadeCodeIndentTest extends JadeTestSupport {

  testRender(
    "val right after a for loop",
    """
ul
  - for(i <- 1 to 2)
    - val doubled = i * 2
    li= doubled
""",
    """
<ul>
  <li>2</li>
  <li>4</li>
</ul>
"""
  )

  // Scala 2 style: the second argument list starts with `{` on the next `-` line
  testRender(
    "second argument list starting with a brace",
    """
- val opt: Option[Int] = Some(3)
- opt.fold
  p none
- { n =>
  p= n
- }
""",
    """
<p>3</p>
"""
  )

  test("generated code is indented once") {
    val source = TemplateSource.fromText(
      "/org/fusesource/scalate/jade/test/indent.jade",
      """
ul
  - for(i <- 1 to 2)
    - val doubled = i * 2
    li= doubled
""".trim
    )
    val lines = engine.generateScala(source).source.linesIterator.toList
    def indentOf(prefix: String): Int = {
      val line = lines.find(_.trim.startsWith(prefix)).getOrElse(fail(s"no line starting with $prefix"))
      line.takeWhile(_ == ' ').length
    }
    val forIndent = indentOf("for(i <- 1 to 2)")
    assert(indentOf("val doubled") == forIndent + 2)
    // the output after the val in the loop body has the same indentation
    assert(indentOf("$_scalate_$_context << ( \"  <li>\"") == forIndent + 2)
  }
}
