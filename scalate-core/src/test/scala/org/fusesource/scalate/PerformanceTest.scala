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

import Asserts._

import java.io.File

class PerformanceTest extends TemplateTestSupport {

  benchmarkTest("custom text") {
    val template = engine.compileSsp("""<%@ val name: String %>
Hello ${name}!
""")

    val output = engine.layout("foo.ssp", template, Map("name" -> "James")).trim
    assertContains(output, "Hello James")
  }

  benchmarkTest("simple.ssp") {
    val output = engine.layout("simple.ssp", Map("name" -> "James")).trim

    assertContains(output, "1 + 2 = 3")
  }

  def benchmarkTest(testName: String)(block: => Unit): Unit = {
    test("benchmark: " + testName) {
      for (i <- 1 to 10) {
        benchmark(testName + " run " + i) {
          block
        }
      }
    }
  }
}