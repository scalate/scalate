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

package org.fusesource.scalate.console

/**
 * @version $Revision: 1.1 $
 */
import _root_.java.io.{OutputStreamWriter, PrintWriter}
import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.util._
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSuite
import _root_.org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EditLinkTest extends FunSuite with Logging {

  val file = "src/test/scala/org/fusesource/scalate/console/EditLinkTest.scala"

  test("default edit link") {
    editLink("default")
  }

  test("IDE edit link") {
    System.setProperty("scalate.editor", "ide")
    editLink("ide")
  }

  def editLink(name: String) = {

    // lets put a render context in scope
    RenderContext.using( new DefaultRenderContext(new TemplateEngine(), new PrintWriter(new OutputStreamWriter(System.out))) ) {
      val link = EditLink.editLink(file)("Edit file")
      println(name + " link = " + link)
    }
  }
}