/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import util.Logging
import Asserts._

/**
 */
@RunWith(classOf[JUnitRunner])
class TemplateEngineTest extends FunSuite with Logging {
  val engine = new TemplateEngine
  engine.workingDirectory = new File("target/test-data/TemplateEngineTest")

  test("load template") {
    val template = engine.compileSsp("""<%@ val name: String %>
Hello ${name}!
""")

    val output = engine.layout(template, Map("name" -> "James")).trim
    assertContains(output, "Hello James")
    fine("template generated: " + output)
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
    val output = engine.layout(template, Map("t" -> classOf[String])).trim
    val lines = output.split('\n')

    for (line <- lines) {
      println("line: " + line)
    }

    expect("<%@ val it : java.lang.String %>") {lines(0)}
    expect("<p>hello ${it} how are you?</p>") {lines(1)}
  }

}
