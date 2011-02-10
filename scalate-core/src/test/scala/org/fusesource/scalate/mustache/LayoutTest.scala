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
package mustache

import org.fusesource.scalate.TemplateTestSupport
import java.io.File
import layout.DefaultLayoutStrategy

/**
 * @version $Revision: 1.1 $
 */
class LayoutTest extends TemplateTestSupport {

  val expected = """<html>
<head>
  <!-- mylayout -->
<title>My Foo Title</title>
</head>
<body>
  <div id="header"></div>
  <div id="content">
  <h1>Some Foo</h1><p>This is some text</p>
  </div>
  <div id="footer"></div>
</body>
</html>"""

  showOutput = false

  test("use mustache layouts of html templates") {
    val output = engine.layout("sample.mustache")

    if (showOutput) {
      println("Generated: ")
      println(output)
    }

    expect(expected) {
      output
    }
  }

  override protected def createTemplateEngine = {
    debug("Using rootDir: %s", rootDir)
    val engine = new TemplateEngine(Some(rootDir))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, "mylayout.mustache")
    engine
  }

  def rootDir = new File(baseDir, "src/test/resources/org/fusesource/scalate/mustache")

}