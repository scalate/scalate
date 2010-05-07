package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.{TemplateSource, TemplateTestSupport}
import org.fusesource.scalate.util.IOUtil

/**
 * Runs the system tests from the mustache.js distro
 */
class MustacheJsSystemTest extends TemplateTestSupport {

  testMustacheJs("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  def testMustacheJs(name: String, attributes: Map[String,Any]): Unit = {
    test(name) {
      val template = engine.compile(TemplateSource.fromFile(new File(rootDir, name + ".html")))
      val expectedOutput = IOUtil.loadTextFile(new File(rootDir, name + ".txt")).trim
      assertOutput(expectedOutput, template, attributes)
    }
  }

  def rootDir = new File(baseDir, "src/test/resources/moustache/js")

  // lets install html as a moustache extension
  engine.codeGenerators += "html" -> new MustacheCodeGenerator


}