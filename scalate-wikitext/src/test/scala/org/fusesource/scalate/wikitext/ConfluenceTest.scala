/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import org.fusesource.scalate.test.FunSuiteSupport

class ConfluenceTest extends FunSuiteSupport {
  val filter = ConfluenceFilter

  test("parse confluence wiki") {
    assertFilter(
"""h1. Title
Hello
* one
* two
""",

"""<h1 id="Title">Title</h1><p>Hello</p><ul><li>one</li><li>two</li></ul>""")
  }

  protected def assertFilter(source: String, expected: String): Unit = {
    println("Converting: " + source)
    val actual = filter.filter(source)
    println("Created: " + actual)
    expect(expected) {actual}
  }

  if (Pygmentize.isInstalled) {
    test("pygmentize macro") {
    assertFilter(
"""
START

  {pygmentize:xml}
  <ul>
    <li>one</li>
    <li>two</li>
  </ul>
  {pygmentize}

END
""",

"""<p>START</p><div class="syntax"><div class="highlight"><pre><span class="nt">&lt;ul&gt;</span>&#x000A;  <span class="nt">&lt;li&gt;</span>one<span class="nt">&lt;/li&gt;</span>&#x000A;  <span class="nt">&lt;li&gt;</span>two<span class="nt">&lt;/li&gt;</span>&#x000A;<span class="nt">&lt;/ul&gt;</span>&#x000A;</pre></div>&#x000A;</div><p>END</p>""")

    }
  } else {
    warn("Pygmentize not installed so ignoring the tests")
  }
}