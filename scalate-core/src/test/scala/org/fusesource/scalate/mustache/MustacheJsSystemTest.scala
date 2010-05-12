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

  testMustacheJs("array_of_strings", Map("array_of_strings" -> List("hello", "world")))
  testMustacheJs("array_of_strings_options", Map("array_of_strings_options" -> List("hello", "world")))

  // Note we had to zap the comments from the sample results - seems bug in mustache.js
  testMustacheJs("comments", Map("title" -> (() => "A Comedy of Errors")))
  testMustacheJs("comments_multi_line", Map("title" -> (() => "A Comedy of Errors")))

  testMustacheJs("complex", Map(
    "header" -> (() => "Colors"),
    "item" -> List(
      Map("name" -> "red", "current" -> true, "url" -> "#Red"),
      Map("name" -> "green", "current" -> false, "url" -> "#Green"),
      Map("name" -> "blue", "current" -> false, "url" -> "#Blue")
      ),
    "link" -> ((s: Scope) => !(s("current").get.asInstanceOf[Boolean])),
    "list" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size > 0),
    "empty" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size == 0)))

  testMustacheJs("crazy_recursive", Map(
    "top_nodes" -> Map(
      "contents" -> "1",
      "children" -> List(Map(
        "contents" -> "2",
        "children" -> List(Map(
          "contents" -> "3",
          "children" -> List()
          ))
        ), Map(
        "contents" -> "4",
        "children" -> List(Map(
          "contents" -> "5",
          "children" -> List(Map(
            "contents" -> "6",
            "children" -> List()
            )
            ))
          ))
        ))
    ))

  testMustacheJs("delimiters", Map(
    "first" -> "It worked the first time.",
    "second" -> "And it worked the second time.",
    "third" -> "Then, surprisingly, it worked the third time.",
    "fourth" -> "Fourth time also fine!."
    ))

  testMustacheJs("double_section", Map("t" -> true, "two" -> "second"))

  testMustacheJs("empty_template", Map())

  // Note that mustache.ruby quotes the &quot; which we follow unlike the mustache.js test case
  testMustacheJs("escaped", Map(
    "title" -> (() => "Bear > Shark"),
    "entities" -> "&quot;"))

  testMustacheJs("error_not_found", Map())

  // TODO allow a Scope to be passed as well, plus allow a function that returns a string to be invoked too
  testMustacheJs("higher_order_sections", Map(
    "name" -> "Tater",
    "helper" -> "To tinker?",
    "bolder" -> ((text: String) => <b>{text}</b> :: Text(" To tinker?") :: Nil)))

  testMustacheJs("inverted_section", Map("repo" -> List()))

  testMustacheJs("null_string", Map("name" -> "Elise",
    "glytch" -> true,
    "binary" -> false,
    "value" -> null,
    "numeric" -> (() => Double.NaN)))


  // TODO use case class
  testMustacheJs("recursive", Map("show" -> false))

  testMustacheJs("recursion_with_same_names", Map(
    "name" -> "name",
    "description" -> "desc",
    "terms" -> List(
      Map("name" -> "t1", "index" -> 0),
      Map("name" -> "t2", "index" -> 1))))

  testMustacheJs("reuse_of_enumerables", Map("terms" -> List(
    Map("name" -> "t1", "index" -> 0),
    Map("name" -> "t2", "index" -> 1))))

  testMustacheJs("section_as_context", Map("a_object" -> Map(
    "title" -> "this is an object",
    "description" -> "one of its attributes is a list",
    "a_list" -> List(
      Map("label" -> "listitem1"),
      Map("label" -> "listitem2")))))

  // Note we use internationalisation by default, so commas introduced in the 1000s in these numbers
  testMustacheJs("simple", Map(
    "name" -> "Chris",
    "value" -> 10000,
    "taxed_value" -> ((s: Scope) => s("value").get.asInstanceOf[Int] * 0.6),
    "in_ca" -> true
    ))


  // Note used the result from mustache.ruby
  //testMustacheJs("template_partial", Map("title" -> (() => "Welcome"), "partial" -> Map("again" -> "Goodbye")))
  testMustacheJs("template_partial", Map("title" -> (() => "Welcome")))

  testMustacheJs("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  testMustacheJs("unescaped", Map("title" -> (() => "Bear > Shark")))
  testMustacheJs("utf8", Map("test" -> "中文"))
  testMustacheJs("unknown_pragma", Map())


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