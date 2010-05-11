package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.{TemplateSource, TemplateTestSupport}
import org.fusesource.scalate.util.IOUtil

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

  testMustacheJs("empty_template", Map())
  testMustacheJs("error_not_found", Map())

  testMustacheJs("escaped", Map("title" -> (() => "Bear > Shark"), "entities" -> "\""))

  testMustacheJs("null_string", Map("name" -> "Elise",
    "glytch" -> true,
    "binary" -> false,
    "value" -> null,
    "numeric" -> (() => Double.NaN)))


  // TODO use case class
  testMustacheJs("reuse_of_enumerables", Map("terms" -> List(
    Map("name" -> "t1", "index" -> 0),
    Map("name" -> "t2", "index" -> 1))))

  testMustacheJs("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  testMustacheJs("unescaped", Map("title" -> (() => "Bear > Shark")))



  // The following are bad test cases that don't seem correct...
  ignore("bad test cases") {

    // TODO should &quot; be ignored from quoting?
    testMustacheJs("escaped", Map("title" -> (() => "Bear > Shark"), "entities" -> "&quot;"))
  }



  // Implementation methods
  //-------------------------------------------------------------------------

  def testMustacheJs(name: String, attributes: Map[String, Any]): Unit = {
    test(name) {
      val template = engine.compile(TemplateSource.fromFile(new File(rootDir, name + ".html")).templateType("mustache"))
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
}