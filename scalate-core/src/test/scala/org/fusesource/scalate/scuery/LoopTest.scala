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
import xml.NodeSeq
class LoopTest extends FunSuiteSupport {
  val people = List(Person("James", "Beckington"), Person("Hiram", "Tampa"))

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
          <tr class="person">
            <td class="name"></td>
            <td class="location"></td>
          </tr>
        </table>
      </div>
      <div id="messages"></div>
      <div id="footer">Footer</div>
    </body>
  </html>


  /*
  test("loop using new transformer on each person") {
    object transformer1 extends Transformer {
      $(".person") { node =>

        people.flatMap { p =>
          new Transformer {
            $(".name").contents = p.name
            $(".location").contents = p.location
          }.apply(node)
        }                                   
      }
    }
    assertTransformed(transformer1(xml))
  }
  */

  test("loop using new Transform statement on each person") {
    object transformer2 extends Transformer {
      $(".person") { node =>
        people.flatMap { p =>
          new Transform(node) {
            $(".name").contents = p.name
            $(".location").contents = p.location
          }
        }
      }
    }
    assertTransformed(transformer2(xml))
  }

  test("loop using transform method on each person") {
    object transformer3 extends Transformer {
      $(".person") { node =>
        people.flatMap { p =>
          transform(node) { $ =>
            $(".name").contents = p.name
            $(".location").contents = p.location
          }
        }
      }
    }
    assertTransformed(transformer3(xml))
  }

  test("loop using transform method with new transformer") {
    object transformer4 extends Transformer {
      $(".person") { node =>
        people.flatMap { p =>
          // TODO how to know what the current ancestor is?
          transform(node, new Transformer {
            $(".name").contents = p.name
            $(".location").contents = p.location
          })
        }
      }
    }
    assertTransformed(transformer4(xml))
  }

  test("loop using NestedTransformer") {
    object transformer5 extends NestedTransformer {
      $(".person") { node =>
        people.flatMap { p =>
          transform(node) { t => 
            $(".name").contents = p.name
            $(".location").contents = p.location
          }
        }
      }
    }
    assertTransformed(transformer5(xml))
  }

  def assertTransformed(result: NodeSeq): Unit = {
    debug("got result: %s", result)

    expect("James") {(result \\ "td")(0).text}
    expect("Beckington") {(result \\ "td")(1).text}

    expect("Hiram") {(result \\ "td")(2).text}
    expect("Tampa") {(result \\ "td")(3).text}
  }
}