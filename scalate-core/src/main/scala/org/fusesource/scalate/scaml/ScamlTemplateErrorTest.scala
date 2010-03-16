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
class ScamlTemplateErrorTest extends FunSuite {

  testRender("valid indenting",
"""
%html
  %two
    %three
  %two
""","""
<html>
  <two>
    <three></three>
  </two>
  <two></two>
</html>
""")
  /////////////////////////////////////////////////////////////////////
  //
  // Tests for indentation inconsistencies 
  //
  /////////////////////////////////////////////////////////////////////

  testInvalidSyntaxException("Inconsistent indent level detected: intended too shallow",
"""
%html
  %two
   %tooshallow
  %two
""",
"Inconsistent indent level detected: intended too shallow at 4.4")

  testInvalidSyntaxException("Inconsistent indent level detected: intended too shallow at root",
"""
%html
  %two
 %toodeep
  %two
""",
"Inconsistent indent level detected: intended too shallow at 4.2")

  testInvalidSyntaxException("Inconsistent indent level detected: intended too deep",
"""
%html
  %two
     %toodeep
  %two
""",
"Inconsistent indent level detected: intended too deep at 4.6")

  testInvalidSyntaxException("Inconsistent indent detected: indented with spaces but previous lines were indented with tabs",
"""
%html
	%tab
  %spaces
	%tab
""",
"Inconsistent indent detected: indented with spaces but previous lines were indented with tabs at 4.3")

  testInvalidSyntaxException("Unexpected comma in html attribute list",
"""
%html
  %tab(comma="common", error="true")
  %p commas in attribute lists is a common errro
""",
"`)' expected but `,' found at 3.22")

  def testInvalidSyntaxException(description:String, template:String, error:String) = {
    test(description) {
      try {
        println(render(template))
        fail("Expected InvalidSyntaxException was not thrown")
      } catch {
        case e:TestFailedException=> throw e
        case e:InvalidSyntaxException=> {
          expect(error) {
            e.getMessage
          }
        }
        case x=>
          fail("Expected InvalidSyntaxException was not thrown.  Instead got a: "+x)
      }
    }
  }

  def ignoreInvalidSyntaxException(description:String, template:String, error:String) = {
    ignore(description) {
    }
  }

  def testRender(description:String, template:String, result:String) = {
    test(description) {
      expect(result) { render(template) }
    }
  }

  def ignoreRender(description:String, template:String, result:String) = {
    ignore(description) {
      expect(result) { render(template) }
    }
  }

  var engine = new TemplateEngine

  def render(content:String): String = {

    engine.resourceLoader = new FileResourceLoader {
      override def load( uri: String ): String = content
      override def lastModified(uri:String): Long = 0
    }

    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = new DefaultRenderContext(engine, out)
    context.attributes("context") = context

    val template = engine.compile("/org/fusesource/scalate/scaml/test.scaml")
    template.render(context)
    out.close
    buffer.toString
  }  
}


