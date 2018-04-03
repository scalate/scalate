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
package org.fusesource.scalate.scuery

import _root_.org.fusesource.scalate.FunSuiteSupport
import xml.Node

class SetAttributeTest extends FunSuiteSupport {
  val xml = <html>
              <body>
                <div id="content">
                  <a href="#" class="foo" title="A foo link">foo link</a>
                  <a href="#" class="bar" title="A bar link">bar link</a>
                  <a href="#" class="jog" title="A jog link">jog link</a>
                </div>
              </body>
            </html>

  test(" transform") {
    object transformer extends Transformer {

      // 3 different approaches to changing attributes
      $("a.foo").attribute("href", "http://scalate.fusesource.org/")

      $("a.bar").attribute("href").value = "http://scalate.fusesource.org/documentation/"

      $("a.jog").attribute("href") {
        e =>
          "http://scalate.fusesource.org/documentation/" + (e \ "@class") + ".html"
      }
    }

    val result = transformer(xml)

    debug("got result: " + result)

    assertLink((result \\ "a")(0), "http://scalate.fusesource.org/", "foo", "A foo link")
    assertLink((result \\ "a")(1), "http://scalate.fusesource.org/documentation/", "bar", "A bar link")
    assertLink((result \\ "a")(2), "http://scalate.fusesource.org/documentation/jog.html", "jog", "A jog link")
  }

  def assertLink(a: Node, href: String, className: String, title: String): Unit = {
    debug("testing link node: " + a)
    assertResult(href) { (a \ "@href").toString }
    assertResult(className) { (a \ "@class").toString }
    assertResult(title) { (a \ "@title").toString }
  }
}
