/**
 * Copyright (C) 2009-2010 the original author or authors.
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

class ExtraImportTest extends TemplateTestSupport {
  test("test template using custom import") {
    val template = engine.compileSsp("""
<%@ val bean: MyBean = null %>
Hello ${if (bean != null) bean else "no bean"}
""")

    val output = engine.layout(template).trim
    assertContains(output, "Hello no bean")
    debug("template generated: " + output)
  }


  override protected def createTemplateEngine = {
    val engine = super.createTemplateEngine
    engine.importStatements ++= List("import org.fusesource.scalate.introspector.MyBean")
    engine
  }
}