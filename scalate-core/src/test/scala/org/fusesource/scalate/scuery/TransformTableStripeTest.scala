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

class TransformTableStripeTest extends FunSuiteSupport {

  val xml = <table class="people">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Location</th>
                </tr>
              </thead>
              <tbody>
                <tr class="person odd">
                  <td class="name">Odd name</td>
                  <td class="location">Odd location</td>
                </tr>
                <tr class="person even">
                  <td class="name">Odd name</td>
                  <td class="location">Odd location</td>
                </tr>
                <tr class="person empty">
                  <td colspan="2">There are no people yet!</td>
                </tr>
              </tbody>
            </table>

  class PersonTransformer(people: List[Person]) extends Transformer {
    $("tbody").contents {
      node =>
        if (people.isEmpty) {
          node.$("tr.empty")
        } else {
          people.zipWithIndex.flatMap {
            case (p, i) =>
              val row = if (i % 2 == 0) node.$("tr.odd") else node.$("tr.even")
              transform(row) {
                $ =>
                  $(".name").contents = p.name
                  $(".location").contents = p.location
              }
          }
        }
    }
  }

  test("stripe table") {
    val transformer = new PersonTransformer(List(Person("James", "Beckington"), Person("Hiram", "Tampa")))
    val result = transformer(xml)
    logger.debug("got result: " + result)

    assertSize("tbody tr", result, 2)
    assertSize("tbody tr.odd", result, 1)
    assertSize("tbody tr.even", result, 1)

    assertText("tbody tr.odd .name", result, "James")
    assertText("tbody tr.even .name", result, "Hiram")
    assertText("tbody tr.odd .location", result, "Beckington")
    assertText("tbody tr.even .location", result, "Tampa")
  }

  test("stripe empty table") {
    val striper = new PersonTransformer(List())
    val result = striper(xml)
    logger.debug("got result: " + result)

    assertSize("tbody tr", result, 1)
    assertSize("tbody tr.empty", result, 1)
  }

}
