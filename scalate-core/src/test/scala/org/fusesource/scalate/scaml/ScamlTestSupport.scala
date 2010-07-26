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

package org.fusesource.scalate.scaml

import _root_.org.fusesource.scalate._
import _root_.org.scalatest.TestFailedException
import _root_.java.util.concurrent.atomic.AtomicInteger
import java.io.{StringWriter, PrintWriter, File}
import support.FileResourceLoader

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ScamlTestSupport extends TemplateTestSupport {
  val testCounter = new AtomicInteger(1)

  val NOOP = ()=>{}

  def testRender(description: String, template: String, result: String, before:()=>Unit = NOOP, after:()=>Unit = NOOP ) = {
    test(description) {
      expect(result.trim) {
        before()
        try {
          render(description, template.trim).trim
        } finally {
          after()
        }
      }
    }
  }

  def ignoreRender(description: String, template: String, result: String, before:()=>Unit = NOOP, after:()=>Unit = NOOP) = {
    ignore(description) {
    }
  }

  def testInvalidSyntaxException(description: String, template: String, error: String) = {
    test(description) {
      try {
        debug(render(description, template.trim).trim)
        fail("Expected InvalidSyntaxException was not thrown")
      } catch {
        case e: TestFailedException => throw e
        case e: InvalidSyntaxException => {
          expect(error) {
            e.getMessage
          }
        }
        case x: Throwable =>
          x.printStackTrace
          fail("Expected InvalidSyntaxException was not thrown.  Instead got a: " + x)
      }
    }
  }

  def ignoreInvalidSyntaxException(description: String, template: String, error: String) = {
    ignore(description) {
    }
  }

  def testCompilerException(description: String, template: String, error: String) = {
    test(description) {
      try {
        debug(render(description, template.trim).trim)
        fail("Expected CompilerException was not thrown")
      } catch {
        case e: TestFailedException => throw e
        case e: CompilerException => {
          expect(error) {
            e.errors.head.message
          }
        }
        case x: Throwable =>
          x.printStackTrace
          fail("Expected CompilerException was not thrown.  Instead got a: " + x)
      }
    }
  }


  def render(name: String, content: String): String = {
    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = new DefaultRenderContext(engine, out) {
      val name = "Hiram"
      val title = "MyPage"
      val href = "http://scalate.fusesource.org"
      val quality = "scrumptious"
    }

    engine.bindings = List(Binding("context", context.getClass.getName, true))

    val testIdx = testCounter.incrementAndGet
    val dir = new File("target/ScamlTest")
    dir.mkdirs
    engine.workingDirectory = dir
    context.attributes("context") = context
    context.attributes("bean") = Bean("red", 10)
    context.attributes("label") = "Scalate"

    val template = compileScaml("/org/fusesource/scalate/scaml/test" + name, content)
    template.render(context)
    out.close
    buffer.toString
  }
}