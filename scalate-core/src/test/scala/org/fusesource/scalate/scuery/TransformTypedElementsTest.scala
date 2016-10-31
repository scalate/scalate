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
import xml.{ NodeSeq }

case class Car(make: String, model: String, color: String)
case class Dog(name: String, breed: String, color: String, age: Int)

class TransformTypedElementsTest extends FunSuiteSupport {
  val car1 = Car("Ford", "SMax", "Silver")
  val car2 = Car("Porsche", "Carerra", "Black")
  val things = List(car1, car2, Dog("Emma", "Labrador", "Golden", 9))

  val xml = <ul class="items">
              <li class="car">
                <img src="/images/car.jpg"/>
                <div class="field">
                  <div class="label">Make</div><div class="make">BMW</div>
                </div>
                <div class="field">
                  <div class="label">Model</div><div class="model">525i</div>
                </div>
                <div class="field">
                  <div class="label">Color</div><div class="color">Blue</div>
                </div>
              </li>
              <li class="dog">
                <img src="/images/dog.jpg"/>
                <div class="field">
                  <div class="label">Name</div><div class="name">Cameron</div>
                </div>
                <div class="field">
                  <div class="label">Breed</div><div class="breed">Bishon Frieze</div>
                </div>
                <div class="field">
                  <div class="label">Color</div><div class="color">White</div>
                </div>
                <div class="field">
                  <div class="label">Age</div><div class="age">7</div>
                </div>
              </li>
            </ul>

  test("transform contents") {
    object transformer extends Transformer {
      $("ul.items").contents {
        node =>
          things.flatMap {
            case c: Car =>
              transform(node.$("li.car")) {
                $ =>
                  $(".make").contents = c.make
                  $(".model").contents = c.model
                  $(".color").contents = c.color
              }
            case d: Dog =>
              transform(node.$("li.dog")) {
                $ =>
                  $(".name").contents = d.name
                  $(".breed").contents = d.breed
                  $(".color").contents = d.color
                  $(".age").contents = d.age
              }
            case _ => Nil
          }
      }
    }

    val result = transformer(xml)
    debug("got result: " + result)

    assertSize("li.car", result, 2)
    assertSize("li.car img", result, 2)
    assertSize("li.dog", result, 1)
    assertSize("li.dog img", result, 1)
    assertSize("img", result, 3)
    assertSize("li.car:first-child", result, 1)
    assertSize("li.car:nth-child(2)", result, 1)

    assertCar("li.car:first-child", result, car1)
    assertCar("li.car:nth-child(2)", result, car2)

    assertText("li.dog .name", result, "Emma")
    assertText("li.dog .breed", result, "Labrador")
    assertText("li.dog .color", result, "Golden")
    assertText("li.dog .age", result, "9")
  }

  protected def assertCar(selector: String, result: NodeSeq, car: Car): Unit = {
    val a = result.$(selector)
    expect(false, "nodes for " + selector + " should not be empty!") { a.isEmpty }

    assertText(".make", a, car.make)
    assertText(".model", a, car.model)
    assertText(".color", a, car.color)
  }
}