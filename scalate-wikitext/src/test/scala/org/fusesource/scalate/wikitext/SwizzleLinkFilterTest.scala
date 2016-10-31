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
package org.fusesource.scalate
package wikitext

import org.fusesource.scalate.test.FunSuiteSupport
import java.io.File

class SwizzleLinkFilterTest extends FunSuiteSupport {

  val transformer = new SwizzleLinkFilter(List(new File(".")), new TemplateEngine().extensions)

  // valid replacements
  testReplaces(
    """hello <a href='building'>Building</a> there!""",
    """hello <a href='building.html'>Building</a> there!"""
  )

  testReplaces(
    """hello <a href="building">Building</a> there!""",
    """hello <a href="building.html">Building</a> there!"""
  )

  testReplaces(
    """hello <a href="building">Building</a> something <a href="source">Source</a> there!""",
    """hello <a href="building.html">Building</a> something <a href="source.html">Source</a> there!"""
  )

  // upper case versions
  testReplaces(
    """hello <A href='building'>Building</A> there!""",
    """hello <A href='building.html'>Building</A> there!"""
  )

  testReplaces(
    """hello <A href="building">Building</A> there!""",
    """hello <A href="building.html">Building</A> there!"""
  )

  testReplaces(
    """hello <A href="building">Building</A> something <A href="source">Source</A> there!""",
    """hello <A href="building.html">Building</A> something <A href="source.html">Source</A> there!"""
  )

  testReplaces(
    """hello <A href="user' guide">Building</A>""",
    """hello <A href="user' guide.html">Building</A>"""
  )

  // should not replace these...
  testReplaces(
    """hello <a href="http://fusesource.com/">FuseSource</a> there!""",
    """hello <a href="http://fusesource.com/">FuseSource</a> there!"""
  )
  testReplaces(
    """hello <link href="css/style.css.html" rel="stylesheet" type="text/css"/> there!""",
    """hello <link href="css/style.css.html" rel="stylesheet" type="text/css"/> there!"""
  )
  testReplaces(
    """hello <script src="foo.js" type="text/javascript"></script> there!""",
    """hello <script src="foo.js" type="text/javascript"></script> there!"""
  )

  protected def testReplaces(html: String, expected: String): Unit = {
    test("replaces: " + html) {

      val context = new DefaultRenderContext("foo.html", new TemplateEngine)
      val answer = transformer.filter(context, html)

      info("converted " + html)
      info("into: " + answer)

      expect(expected) { answer }
    }
  }
}