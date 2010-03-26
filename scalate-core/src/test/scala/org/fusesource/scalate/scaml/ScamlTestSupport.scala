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
package org.fusesource.scalate.scaml

import _root_.org.scalatest.{TestFailedException, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.fusesource.scalate._
import java.io.{StringWriter, PrintWriter, File}
/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class ScamlTestSupport extends FunSuite {

  var engine = new TemplateEngine

  def testRender(description:String, template:String, result:String) = {
    test(description) {
      expect(result.trim) { render(template.trim).trim }
    }
  }

  def ignoreRender(description:String, template:String, result:String) = {
    ignore(description) {
    }
  }

  def testInvalidSyntaxException(description:String, template:String, error:String) = {
    test(description) {
      try {
        println(render(template.trim).trim)
        fail("Expected InvalidSyntaxException was not thrown")
      } catch {
        case e:TestFailedException=> throw e
        case e:InvalidSyntaxException=> {
          expect(error) {
            e.getMessage
          }
        }
        case x:Throwable=>
          x.printStackTrace
          fail("Expected InvalidSyntaxException was not thrown.  Instead got a: "+x)
      }
    }
  }

  def ignoreInvalidSyntaxException(description:String, template:String, error:String) = {
    ignore(description) {
    }
  }
  def testCompilerException(description:String, template:String, error:String) = {
    test(description) {
      try {
        println(render(template.trim).trim)
        fail("Expected CompilerException was not thrown")
      } catch {
        case e:TestFailedException=> throw e
        case e:CompilerException=> {
          expect(error) {
            e.errors.head.message
          }
        }
        case x:Throwable=>
          x.printStackTrace
          fail("Expected CompilerException was not thrown.  Instead got a: "+x)
      }
    }
  }


  def render(content:String): String = {

    engine.resourceLoader = new FileResourceLoader {
      override def load( uri: String ): String = content
      override def lastModified(uri:String): Long = 0
    }

    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = new DefaultRenderContext(engine, out) {
      val name = "Hiram"
      val title = "MyPage"
      val href = "http://scalate.fusesource.org"
      val quality = "scrumptious"
    }

    engine.bindings = List(Binding("context", context.getClass.getName, true))

    context.attributes("context") = context
    context.attributes("bean") = Bean("red", 10)
    context.attributes("label") = "Scalate"

    val template = engine.compile("/org/fusesource/scalate/scaml/test.scaml")
    template.render(context)
    out.close
    buffer.toString
  }
}