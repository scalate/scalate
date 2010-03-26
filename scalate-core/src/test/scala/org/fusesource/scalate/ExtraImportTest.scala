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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import java.io.File
import util.Logging
import Asserts._

@RunWith(classOf[JUnitRunner])
class ExtraImportTest extends FunSuite with Logging {
  val engine = new TemplateEngine
  engine.workingDirectory = new File("target/test-data/ExtraImportTest")
  engine.importStatements ++= List("import org.fusesource.scalate.introspector.MyBean")

  test("test template using custom import") {
    val template = engine.compileSsp("""
<%@ val bean: MyBean = null %>
Hello ${if (bean != null) bean else "no bean"}
""")

    val output = engine.layout(template).trim
    assertContains(output, "Hello no bean")
    debug("template generated: " + output)
  }
}