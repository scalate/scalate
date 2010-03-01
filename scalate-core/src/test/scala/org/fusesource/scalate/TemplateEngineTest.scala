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

  // This is failing on windows right now due to using a 
  // temp in the implementation.
  ignore("load template") {
    val template = engine.compileSsp("""<%@ val name: String %>
Hello ${name}!
""")

    val output = engine.layout(template, Map("name" -> "James")).trim
    assertContains(output, "Hello James")
    fine("template generated: " + output)
  }

}
