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

import java.io.File

import Asserts._
import org.fusesource.scalate.support.StringTemplateSource
class TemplateEngineTest extends FunSuiteSupport {

  val engine = new TemplateEngine
  engine.workingDirectory = new File(baseDir, "target/test-data/TemplateEngineTest")


  test("load file template") {
    val template = engine.load(new File(baseDir, "src/test/resources/simple.ssp"))
    val output = engine.layout("foo0.ssp", template).trim

    assertContains(output, "1 + 2 = 3")
  }

  test("string template with custom bindings") {
    val source = "hello ${name}"
    val template = engine.compileSsp(source, List(Binding("name", "String")))
    val output = engine.layout("foo1.ssp", template, Map("name" -> "James"))

    expect("hello James") {output}
  }


  test("string template with attributes") {
    val source = "<%@ val name: String %> hello ${name}"

    val template = engine.compileSsp(source)
    val output = engine.layout("foo2.ssp", template, Map("name" -> "Hiram"))

    expect("hello Hiram") {output.trim}
  }

  test("load template") {
    val template = engine.compileSsp("""
<%@ val name: String %>
Hello ${name}!
""")

    val output = engine.layout("foo3.ssp", template, Map("name" -> "James")).trim
    assertContains(output, "Hello James")
    debug("template generated: " + output)
  }

  test("throws ResourceNotFoundException if template file does not exist") {
    intercept[ResourceNotFoundException] {
      engine.load("does-not-exist.ssp", Nil)
    }
  }

  test("escape template") {
    val templateText = """<%@ val t: Class[_] %>
<%@ val name: String = "it" %>
\<%@ val ${name} : ${t.getName} %>
<p>hello \${${name}} how are you?</p>
"""
    val template = engine.compileSsp(templateText)
    val output = engine.layout("foo4.ssp", template, Map("t" -> classOf[String])).trim
    val lines = output.split('\n')

    for (line <- lines) {
      debug("line: " + line)
    }

    expect("<%@ val it : java.lang.String %>") {lines(0)}
    expect("<p>hello ${it} how are you?</p>") {lines(1)}
  }

  test("cache invalidation test") {

    val source = "<%@ val name: String %> hello ${name}"
    val sourceUpdated = "<%@ val name: String %> hello world, it's ${name}"
    val sourceUpdated2 = "<%@ val name: String %> hello world 2, it's ${name}"

    val cachedEngine = new TemplateEngine
    cachedEngine.workingDirectory = new File(baseDir, "target/test-data/TemplateEngineTestCache")

    cachedEngine.allowCaching = true
    cachedEngine.allowReload = false

    val template = new StringTemplateSource(s"cached.ssp", source)
    val templateUpdated = new StringTemplateSource(s"cached.ssp", sourceUpdated)
    val templateUpdated2 = new StringTemplateSource(s"cached.ssp", sourceUpdated2)

    cachedEngine.expireAndCompile(template)

    val res = cachedEngine.layout(template, Map("name" -> "Fatih"))

    assertResult(" hello Fatih")(res)

    val res2 = cachedEngine.layout(templateUpdated, Map("name" -> "Fatih2"))

    assertResult(" hello Fatih2")(res2)

    cachedEngine.expireAndCompile(templateUpdated)

    val res3 = cachedEngine.layout(templateUpdated, Map("name" -> "Fatih3"))

    assertResult(" hello world, it's Fatih3")(res3)

    val res4 = cachedEngine.layout(templateUpdated2, Map("name" -> "Fatih4"))

    assertResult(" hello world, it's Fatih4")(res4)

  }
}
