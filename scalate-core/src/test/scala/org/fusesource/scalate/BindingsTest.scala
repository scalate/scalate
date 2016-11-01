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

class BindingsTest extends TemplateTestSupport {
  test("can use a template with default binding expression") {
    val responseClassName = classOf[DummyResponse].getName
    engine.bindings = List(
      Binding("context", classOf[DefaultRenderContext].getName, true, isImplicit = true),
      Binding("response", responseClassName, defaultValue = Some("new " + responseClassName + "()"))
    )

    val text = engine.layout(TemplateSource.fromText("foo.ssp", "hello ${response}"))

    info("Got: " + text)
  }

  test("Int binding with a default value") {
    engine.bindings = List(Binding("year", "Int", false, Some("1970")))

    val text1 = engine.layout(TemplateSource.fromText("foo2.ssp", "${year.toString}'s is the hippies era"))
    info("Got: " + text1)
    assertResult("1970's is the hippies era") { text1.trim }

    val text2 = engine.layout(TemplateSource.fromText("foo3.ssp", "${year.toString}'s is the hippies era"), Map("year" -> 1950))
    info("Got: " + text2)
    assertResult("1950's is the hippies era") { text2.trim }
  }
}

class DummyResponse {
  def setContentType(value: String): Unit = {}
}