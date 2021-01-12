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
package org.fusesource.scalate.wikitext

import org.fusesource.scalate._
import org.fusesource.scalate.test.FunSuiteSupport

class TextileTest extends FunSuiteSupport {

  val filter = TextileFilter

  protected def renderTextile(source: String): String = {
    logger.debug("Converting: " + source)
    val context = new DefaultRenderContext("foo.textile", new TemplateEngine())
    val actual = filter.filter(context, source)
    logger.info("Generated: " + actual)

    logger.info(actual)
    actual
  }

  protected def assertFilter(source: String, expected: String): Unit = {
    val actual: String = renderTextile(source)
    assertResult(expected) { actual }
  }

  test("parse textile markup") {
    assertFilter(
      """h1. Title

Hello
* one
* two
""",

      """<h1 id="Title">Title</h1><p>Hello</p><ul><li>one</li><li>two</li></ul>""")
  }

}
