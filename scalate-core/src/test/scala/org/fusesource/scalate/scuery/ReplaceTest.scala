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

class ReplaceTest extends FunSuiteSupport {
  val xml = <html>
    <body>
      <div id="content">
        <a href="#" class="foo" title="A link">Some Link</a>
      </div>
    </body>
  </html>



  test(" transform") {
    object transformer extends Transformer {
      $("a.foo") {
        n =>
          <a href="http://scalate.fusesource.org/" class={n \ "@class"} title={n \ "@title"}>
            {n.text}
          </a>
      }
    }

    val result = transformer(xml)

    debug("got result: " + result)

    val a = (result \\ "a")(0)
    expect("http://scalate.fusesource.org/") {(a \ "@href").toString}
    expect("foo") {(a \ "@class").toString}
    expect("A link") {(a \ "@title").toString}
  }
}