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

import org.fusesource.scalate.TemplateTestSupport

/**
 * @version $Revision: 1.1 $
 */
class MustacheTest extends TemplateTestSupport {

  test("plain text") {
    assertMoustacheOutput("Hey James!", "Hey James!", Map("name" -> "Foo"))
  }

  test("simple variables") {
    assertMoustacheOutput("Hey James!", "Hey {{name}}!", Map("name" -> "James"))
  }

  ignore("non false not a list") {
    assertMoustacheOutput(
      "Hi James! ",
      "{{#person?}} Hi {{name}}! {{/person?}}",
      Map("person?" -> Map("name" -> "James")))
  }

  test("non false not a list with simple name") {
    assertMoustacheOutput(
      "Hi James ",
      "{{#person}} Hi {{name}} {{/person}}",
      Map("person" -> Map("name" -> "James")))
  }
}