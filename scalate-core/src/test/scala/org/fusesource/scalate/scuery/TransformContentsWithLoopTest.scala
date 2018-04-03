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
import _root_.scala.xml.Node

class TransformContentsWithLoopTest extends FunSuiteSupport {
  val people = List(Person("James", "Beckington"), Person("Hiram", "Tampa"))

  val xml = <ul class="people">
              <li>
                <a href="#" class="person">A person</a>
              </li>
              <li>
                <a href="#" class="person">Another person</a>
              </li>
            </ul>

  test("transform contents") {

    object transformer extends Transformer {
      $(".people").contents {
        node =>
          people.flatMap {
            p =>
              transform(node.$("li:first-child")) {
                $ =>
                  $("a.person").contents = p.name
                  $("a.person").attribute("href").value = "http://acme.com/bookstore/" + p.name
              }
          }
      }
    }

    val result = transformer(xml)
    debug("got result: " + result)

    assertPersonLink((result \ "li" \ "a")(0), "James")
    assertPersonLink((result \ "li" \ "a")(1), "Hiram")
  }

  protected def assertPersonLink(a: Node, name: String): Unit = {
    debug("Testing " + a + " for name: " + name)
    assertResult(name) { a.text }
    assertResult("http://acme.com/bookstore/" + name) { (a \ "@href").toString }
  }
}
