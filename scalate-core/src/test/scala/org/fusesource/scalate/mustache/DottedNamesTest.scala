/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.mustache

/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.fusesource.scalate.TemplateTestSupport

/**
 * Based on Mustache spec for Dotted Names:
 * https://github.com/mustache/spec/blob/master/specs/interpolation.yml#L142
 *
 * @version $Revision: 1.1 $
 */
class DottedNamesTest extends TemplateTestSupport {
  // Dotted names should be considered a form of shorthand for sections.
  test("Basic Interpolation") {
    assertMoustacheOutput("'Joe' == 'Joe'", "'{{person.name}}' == '{{#person}}{{name}}{{/person}}'", Map("person" -> Map("name" -> "Joe")))
  }

  // Dotted names should be considered a form of shorthand for sections.
  test("Triple Mustache Interpolation") {
    val data = Map("person" -> Map("name" -> "Joe"))
    case class Person(name: String)
    assertMoustacheOutput("'Joe' == 'Joe'", "'{{{person.name}}}' == '{{#person}}{{{name}}}{{/person}}'", data)
  }

  test("Nested section") {
    assertMoustacheOutput("'Joe' == 'Joe'", "'{{details.person.name}}' == '{{#details}}{{#person}}{{name}}{{/person}}{{/details}}'", Map("details" -> Map("person" -> Map("name" -> "Joe"))))
  }

  test("Dotted name section") {
    assertMoustacheOutput("'Joe' == 'Joe'", "'{{details.person.name}}' == '{{#details.person}}{{name}}{{/details.person}}'", Map("details" -> Map("person" -> Map("name" -> "Joe"))))
  }

  // Dotted names should be considered a form of shorthand for sections.
  test("Triple Mustache Interpolation - object") {
    case class Person(name: String)

    val data = Map("person" -> Person("Joe"))
    assertMoustacheOutput("'Joe' == 'Joe'", "'{{{person.name}}}' == '{{#person}}{{{name}}}{{/person}}'", data)
  }

  // Dotted names should be considered a form of shorthand for sections.
  test("Ampersand Interpolation") {
    assertMoustacheOutput("'Joe' == 'Joe'", "'{{&person.name}}' == '{{#person}}{{&name}}{{/person}}'", Map("person" -> Map("name" -> "Joe")))
  }

  // Dotted names should be functional to any level of nesting.
  test("Arbitrary Depth") {
    assertMoustacheOutput("'Phil' == 'Phil'", "'{{a.b.c.d.e.name}}' == 'Phil'", Map("a" -> Map("b" -> Map("c" -> Map("d" -> Map("e" -> Map("name" -> "Phil")))))))
  }

  // Any falsey value prior to the last part of the name should yield "".
  test("Broken Chains") {
    assertMoustacheOutput("'' == ''", "'{{a.b.c}}' == ''", Map("a" -> Map()))
  }

  // Each part of a dotted name should resolve only against its parent.
  test("Broken Chain Resolution") {
    val data = Map(
      "a" -> Map("b" -> Map()),
      "c" -> Map("name" -> "Jim"))
    assertMoustacheOutput("'' == ''", "'{{a.b.c.name}}' == ''", data)
  }

  // The first part of a dotted name should resolve as any other name.
  test("Initial Resolution") {
    val data =
      Map(
        "a" -> Map("b" -> Map("c" -> Map("d" -> Map("e" -> Map("name" -> "Phil"))))),
        "b" -> Map("c" -> Map("d" -> Map("e" -> Map("name" -> "Wrong")))))
    assertMoustacheOutput("'Phil' == 'Phil'", "'{{#a}}{{b.c.d.e.name}}{{/a}}' == 'Phil'", data)
  }

  // Dotted names should be resolved against former resolutions.
  test("Context Precedence") {
    val data = Map(
      "a" -> Map("b" -> Map()),
      "b" -> Map("c" -> "ERROR"))
    assertMoustacheOutput("", "{{#a}}{{b.c}}{{/a}}", data)
  }

  test("Context Precedence2") {
    val data = Map(
      "a" -> Map("b" -> "James"),
      "b" -> "ERROR")
    assertMoustacheOutput("James", "{{#a}}{{b}}{{/a}}", data)
  }

  test("List test 1") {
    val names = List("Hiram", "James")

    assertMoustacheOutput(
      "start <Hiram> <James> end",
      "start {{#names}}<{{.}}> {{/names}}end",
      Map("names" -> names))
  }

  test("List test 2") {
    val persons = List(Map("name" -> "Hiram"), Map("name" -> "James"))

    assertMoustacheOutput(
      "start <Hiram> <James> end",
      "start {{#persons}}<{{name}}> {{/persons}}end",
      Map("persons" -> persons))
  }

  test("List test 3") {
    val persons = List(Map("name" -> "Hiram"), Map("name" -> "James"))

    assertMoustacheOutput(
      "start <Hiram> <James> end",
      "start {{#all.persons}}<{{name}}> {{/all.persons}}end",
      Map("all" -> Map("persons" -> persons)))
  }

}

