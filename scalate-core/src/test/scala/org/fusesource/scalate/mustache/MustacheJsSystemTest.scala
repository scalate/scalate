package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.{TemplateEngine, TemplateSource, TemplateTestSupport}
import org.fusesource.scalate.util.IOUtil
import java.lang.String
import collection.immutable.Map

/**
 * Runs the system tests from the mustache.js distro
 */
class MustacheJsSystemTest extends TemplateTestSupport {
  var trimOutputAndTemplate = true

  // Note we had to zap the comments from the sample results - seems bug in mustache.js
  testMustacheJs("comments", Map("title" -> (() => "A Comedy of Errors")))
  testMustacheJs("comments_multi_line", Map("title" -> (() => "A Comedy of Errors")))

  testMustacheJs("complex", Map("header" -> (() => "Colors"),
    "item" -> List(
      Map("name" -> "red", "current" -> true, "url" -> "#Red"),
      Map("name" -> "green", "current" -> false, "url" -> "#Green"),
      Map("name" -> "blue", "current" -> false, "url" -> "#Blue")
      ),
    "link" -> ((s: Scope) => !(s("current").get.asInstanceOf[Boolean])),
    "list" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size > 0),
    "empty" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size == 0)))

  testMustacheJs("double_section", Map("t" -> true, "two" -> "second"))
  
  testMustacheJs("empty_template", Map())

  // Note that mustache.ruby quotes the &quot; which we follow unlike the mustache.js test case
  testMustacheJs("escaped", Map("title" -> (() => "Bear > Shark"), "entities" -> "&quot;"))

  testMustacheJs("error_not_found", Map())

  testMustacheJs("null_string", Map("name" -> "Elise",
    "glytch" -> true,
    "binary" -> false,
    "value" -> null,
    "numeric" -> (() => Double.NaN)))


  // TODO use case class
  testMustacheJs("reuse_of_enumerables", Map("terms" -> List(
    Map("name" -> "t1", "index" -> 0),
    Map("name" -> "t2", "index" -> 1))))

  // Note used the result from mustache.ruby
  testMustacheJs("template_partial", Map("title" -> (() => "Welcome")))
  //testMustacheJs("template_partial", Map("title" -> (() => "Welcome"), "partial" -> Map("again" -> "Goodbye")))

  testMustacheJs("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  testMustacheJs("unescaped", Map("title" -> (() => "Bear > Shark")))



  // Implementation methods
  //-------------------------------------------------------------------------

  def testMustacheJs(name: String, attributes: Map[String, Any]): Unit = {
    test(name) {
      debug("Using template reasource loader: " + engine.resourceLoader)
      
      val template = engine.load(engine.source(name + ".html", "mustache"))
      val expectedOutput = IOUtil.loadTextFile(new File(rootDir, name + ".txt"))
      if (trimOutputAndTemplate) {
        assertTrimOutput(expectedOutput.trim, template, attributes)
      }
      else {
        assertOutput(expectedOutput, template, attributes)
      }
    }
  }


  override protected def createTemplateEngine = {
    debug("Using rootDir: " + rootDir)
    new TemplateEngine(Some(rootDir))
  }
  
  def rootDir = new File(baseDir, "src/test/resources/moustache/js")
}