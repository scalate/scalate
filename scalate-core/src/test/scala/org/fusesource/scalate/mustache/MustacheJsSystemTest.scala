package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.{TemplateSource, TemplateTestSupport}
import org.fusesource.scalate.util.IOUtil

/**
 * Runs the system tests from the mustache.js distro
 */
class MustacheJsSystemTest extends TemplateTestSupport {
  var trimOutputAndTemplate = true

  testMustacheJs("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  // TODO use case class
  testMustacheJs("reuse_of_enumerables", Map("terms" -> List(
    Map("name" -> "t1", "index" -> 0),
    Map("name" -> "t2", "index" -> 1))))



  // Implementation methods
  //-------------------------------------------------------------------------

  def testMustacheJs(name: String, attributes: Map[String,Any]): Unit = {
    test(name) {
      val template = engine.compile(TemplateSource.fromFile(new File(rootDir, name + ".html")))
      val expectedOutput = IOUtil.loadTextFile(new File(rootDir, name + ".txt"))
      if (trimOutputAndTemplate) {
        assertTrimOutput(expectedOutput.trim, template, attributes)
      }
      else {
        assertOutput(expectedOutput, template, attributes)
      }
    }
  }

  def rootDir = new File(baseDir, "src/test/resources/moustache/js")

  // lets install html as a moustache extension
  engine.codeGenerators += "html" -> new MustacheCodeGenerator


}