/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.mustache

import java.util.Locale
import collection.immutable.Map

case class Item(name: String, current: Boolean, url: String) {
  def link = !current
}

case class Complex(header: String, item: List[Item]) {
  def list = !empty

  def empty = item.isEmpty
}

case class HigherOrder(name: String, helper: String) {
  def bolder(text: String) = <b>
                               { text }
                             </b> :: Text(" " + helper) :: Nil
}

/**
 * Runs the system tests from the mustache.js distro
 */
class MustacheJsSystemTest extends MustacheTestSupport {
  Locale.setDefault(Locale.US)
  // TODO FixMe
  val runFailingTests = false

  mustacheTest("array_of_strings", Map("array_of_strings" -> List("hello", "world")))
  mustacheTest("array_of_strings_options", Map("array_of_strings_options" -> List("hello", "world")))

  // Note we had to zap the comments from the sample results - seems bug in mustache.js
  mustacheTest("comments", Map("title" -> (() => "A Comedy of Errors")))
  mustacheTest("comments_multi_line", Map("title" -> (() => "A Comedy of Errors")))

  mustacheTest("complex", "map", Map(
    "header" -> (() => "Colors"),
    "item" -> List(
      Map("name" -> "red", "current" -> true, "url" -> "#Red"),
      Map("name" -> "green", "current" -> false, "url" -> "#Green"),
      Map("name" -> "blue", "current" -> false, "url" -> "#Blue")),
    "link" -> ((s: Scope) => !(s("current").get.asInstanceOf[Boolean])),
    "list" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size > 0),
    "empty" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size == 0)))

  mustacheTest("complex", "case class", Map(
    "header" -> (() => "Colors"),
    "item" -> List(
      Item("red", true, "#Red"),
      Item("green", false, "#Green"),
      Item("blue", false, "#Blue")),
    "list" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size > 0),
    "empty" -> ((s: Scope) => s("item").get.asInstanceOf[List[_]].size == 0)))

  mustacheTest("complex", "nested case class", Map(
    "it" -> Complex("Colors", List(
      Item("red", true, "#Red"),
      Item("green", false, "#Green"),
      Item("blue", false, "#Blue")))))

  mustacheTest("crazy_recursive", Map(
    "top_nodes" -> Map(
      "contents" -> "1",
      "children" -> List(Map(
        "contents" -> "2",
        "children" -> List(Map(
          "contents" -> "3",
          "children" -> List()))), Map(
        "contents" -> "4",
        "children" -> List(Map(
          "contents" -> "5",
          "children" -> List(Map(
            "contents" -> "6",
            "children" -> List())))))))))

  mustacheTest("delimiters", Map(
    "first" -> "It worked the first time.",
    "second" -> "And it worked the second time.",
    "third" -> "Then, surprisingly, it worked the third time.",
    "fourth" -> "Fourth time also fine!."))

  mustacheTest("double_section", Map("t" -> true, "two" -> "second"))

  mustacheTest("empty_template", Map())

  // Note that mustache.ruby quotes the &quot; which we follow unlike the mustache.js test case
  mustacheTest("escaped", Map(
    "title" -> (() => "Bear > Shark"),
    "entities" -> "&quot;"))

  mustacheTest("error_not_found", Map())

  if (runFailingTests) {
    mustacheTest("higher_order_sections", "map", Map(
      "name" -> "Tater",
      "helper" -> "To tinker?",
      "bolder" -> ((text: String) => <b>
                                       { text }
                                     </b> :: Text(" To tinker?") :: Nil)))

    mustacheTest("higher_order_sections", "case class with string functor", Map(
      "it" -> HigherOrder("Tater", "To tinker?")))
  }

  mustacheTest("inverted_section", Map("repo" -> List()))

  mustacheTest("null_string", Map(
    "name" -> "Elise",
    "glytch" -> true,
    "binary" -> false,
    "value" -> null,
    "numeric" -> (() => Double.NaN)))

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
    "in_ca" -> true))

  // Note used the result from mustache.ruby
  //mustacheTest("template_partial", Map("title" -> (() => "Welcome"), "partial" -> Map("again" -> "Goodbye")))
  mustacheTest("template_partial", Map("title" -> (() => "Welcome")))

  mustacheTest("two_in_a_row", Map("name" -> "Joe", "greeting" -> "Welcome"))

  mustacheTest("unescaped", Map("title" -> (() => "Bear > Shark")))
  mustacheTest("utf8", Map("test" -> "中文"))
  mustacheTest("unknown_pragma", Map())
}
