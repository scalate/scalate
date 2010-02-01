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
package org.fusesource.scalate.shaml

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import collection.mutable.HashMap
import org.scalatest.FunSuite
import org.fusesource.scalate._
import java.io.{StringWriter, PrintWriter, File}
import util.XmlEscape

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class ShamlTemplateTest extends FunSuite {

  test("simple") {
    expect(
"""
<html>
  <body>
    <p>test</p>
  </body>
</html>
"""
      ) {render(
"""
%html
  %body
    %p test
"""
      )}
  }

  def render(content:String): String = {
    var engine = new TemplateEngine
    engine.workingDirectoryRoot = new File("target/test-data/"+(this.getClass.getName))

    engine.resourceLoader = new FileResourceLoader {
      override def load( uri: String ): String = content
      override def lastModified(uri:String): Long = 0
    }

    val context = new Context()
    val template = engine.loadTemporary("/test.shaml", TemplateArg("context", context.getClass.getName, true))
    template.renderTemplate(context, Map("context"->context))
    context.buffer.toString
  }

}

/**
 * A default template context for use outside of servlet environments
 */
case class Context() extends RenderCollector() {

  val buffer = new StringWriter()
  val out = new PrintWriter(buffer)

  def <<(value: Any): Unit = {
    out.print(value.toString)
  }

  def <<<(value: Any): Unit = {
    out.print(XmlEscape.escape(value.toString))
  }

}
