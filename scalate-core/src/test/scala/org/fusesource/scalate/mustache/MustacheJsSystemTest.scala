package org.fusesource.scalate.mustache

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.IOUtil
import collection.immutable.Map

/**
 * Runs the system tests from the mustache.js distro
 */
class MustacheJsSystemTest extends MustacheTestSupport {

  mustacheTest("array_of_strings", Map("array_of_strings" -> List("hello", "world")))
  mustacheTest("array_of_strings_options", Map("array_of_strings_options" -> List("hello", "world")))

  // Note we had to zap the comments from the sample results - seems bug in mustache.js
  mustacheTest("comments", Map("title" -> (() => "A Comedy of Errors")))
  mustacheTest("comments_multi_line", Map("title" -> (() => "A Comedy of Errors")))

  mustacheTest("complex", Map(
    "header" -> (() => "Colors"),
    "item" -> List(
      Map("name" -> "red", "current" -> true, "url" -> "#Red"),
      Map("name" -> "green", "current" -> false, "url" -> "#Green"),
      Map("name" -> "blue", "current" -> false, "url" -> "#Blue")
      ),
    "link" -> ((s: Scope) => !(s("current").get.asInstanceOf[Boolean])),
    "list" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size > 0),
    "empty" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size == 0)))

  mustacheTest("crazy_recursive", Map(
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

  mustacheTest("delimiters", Map(
    "first" -> "It worked the first time.",
    "second" -> "And it worked the second time.",
    "third" -> "Then, surprisingly, it worked the third time.",
    "fourth" -> "Fourth time also fine!."
    ))

  mustacheTest("double_section", Map("t" -> true, "two" -> "second"))

  mustacheTest("empty_template", Map())

  // Note that mustache.ruby quotes the &quot; which we follow unlike the mustache.js test case
  mustacheTest("escaped", Map(
    "title" -> (() => "Bear > Shark"),
    "entities" -> "&quot;"))

  mustacheTest("error_not_found", Map())

  // TODO allow a Scope to be passed as well, plus allow a function that returns a string to be invoked too
  mustacheTest("higher_order_sections", Map(
    "name" -> "Tater",
    "helper" -> "To tinker?",
    "bolder" -> ((text: String) => <b>{text}</b> :: Text(" To tinker?") :: Nil)))

  mustacheTest("inverted_section", Map("repo" -> List()))

  mustacheTest("null_string", Map("name" -> "Elise",
    "glytch" -> true,
    "binary" -> false,
    "value" -> null,
    "numeric" -> (() => Double.NaN)))


  // TODO use case class
  mustacheTest("recursive", Map("show" -> false))

  mustacheTest("recursion_with_same_names", Map(
    "name" -> "name",
    "description" -> "desc",
    "terms" -> List(
      Map("name" -> "t1", "index" -> 0),
      Map("name" -> "t2", "index" -> 1))))

  mustacheTest("reuse_of_enumerables", Map("terms" -> List(
    Map("name" -> "t1", "index" -> 0),
    Map("name" -> "t2", "index" -> 1))))

  mustacheTest("section_as_context", Map("a_object" -> Map(
    "title" -> "this is an object",
    "description" -> "one of its attributes is a list",
    "a_list" -> List(
      Map("label" -> "listitem1"),
      Map("label" -> "listitem2")))))

  // Note we use internationalisation by default, so commas introduced in the 1000s in these numbers
  mustacheTest("simple", Map(
    "name" -> "Chris",
    "value" -> 10000,
    "taxed_value" -> ((s: Scope) => s("value").get.asInstanceOf[Int] * 0.6),
    "in_ca" -> true
    ))


  // Note used the result from mustache.ruby
  //mustacheTest("template_partial", Map("title" -> (() => "Welcome"), "partial" -> Map("again" -> "Goodbye")))
  mustacheTest("template_partial", Map("title" -> (() => "Welcome")))

  mustacheTest("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  mustacheTest("unescaped", Map("title" -> (() => "Bear > Shark")))
  mustacheTest("utf8", Map("test" -> "中文"))
  mustacheTest("unknown_pragma", Map())
}