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
class TransformTest extends FunSuiteSupport {

  var printOutput = false
  val xml = <html>
              <head>
                <title>My Title</title>
              </head>
              <body>
                <div id="header">Header</div>
                <div id="content">
                  <table class="people">
                    <tr>
                      <th>Name</th>
                      <th>Location</th>
                    </tr>
                    <tr>
                      <td class="name">James</td>
                      <td class="location">Beckington</td>
                    </tr>
                  </table>
                </div>
                <div id="messages"></div>
                <div id="footer">Footer</div>
              </body>
            </html>

  test("try simple transform") {

    object transformer extends Transformer {

      $("table .name").contents = "Hiram"
      $(".location").contents = <b>Tampa</b>
    }

    val result = transformer(xml)
    logger.debug("got result: " + result)

    assertResult("Hiram") { (result \\ "td")(0).text }
    assertResult("Tampa") { (result \\ "td" \\ "b")(0).text }
  }

}
