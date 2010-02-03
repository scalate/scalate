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
import org.scalatest.FunSuite
import org.fusesource.scalate._
import java.io.{StringWriter, PrintWriter, File}
/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class ShamlTemplateTest extends FunSuite {

  test("'%tag' renders a start and end tag") {
    expect(
"""
<html></html>
"""
    ) {render(
"""
%html
"""
    )}
  }

  test("'%tag/' renders a closed tag") {
    expect(
"""
<html/>
"""
    ) {render(
"""
%html/
"""
    )}
  }

  test("'%tag#i1' renders a tag with an id") {
    expect(
"""
<html id="i1"></html>
"""
    ) {render(
"""
%html#i1
"""
    )}
  }

  test("'%tag#i1#i2' last id specified wins") {
    expect(
"""
<html id="i2"></html>
"""
    ) {render(
"""
%html#i1#i2
"""
    )}
  }

  test("'%tag.c1' renders a tag with a class") {
    expect(
"""
<html class="c1"></html>
"""
    ) {render(
"""
%html.c1
"""
    )}
  }

  test("'%tag.c1.c2' renders a tag with multiple classes") {
    expect(
"""
<html class="c1 c2"></html>
"""
    ) {render(
"""
%html.c1.c2
"""
    )}
  }

  test("'.c1' if tag name is omitted, it defaults to div") {
    expect(
"""
<div class="c1"></div>
"""
    ) {render(
"""
.c1
"""
    )}
  }

  test("'%tag{:name => \"value\"}' tag attributes can be specified using a ruby hash syntax") {
    expect(
"""
<html k1="v1" k2="v2"></html>
"""
    ) {render(
"""
%html{:k1=>"v1", "k2"=>"v2"}
"""
    )}
  }

  test("The '%tag{:name => \"value\"}' attribute syntax can span multiple lines after the comma") {
    expect(
"""
<html>
  <body k1="v1" k2="v2"></body>
</html>
"""
    ) {render(
"""
%html
  %body{:k1=>"v1",
"k2"=>"v2"}
"""
    )}
  }

  test("'%tag(name=\"value\")' tag attributes can be specified html attribute syntax") {
    expect(
"""
<html k1="v1" k2="v2"></html>
"""
    ) {render(
"""
%html(k1="v1" k2="v2")
"""
    )}
  }

  test("The '%tag(name=\"value\")' attribute syntax can span multiple lines") {
    expect(
"""
<html>
  <body k1="v1" k2="v2"></body>
</html>
"""
    ) {render(
"""
%html
  %body(k1="v1"
k2="v2")
"""
    )}
  }

  test("Plain text is rendered as plain text") {
    expect(
"""
this is
plain text
"""
    ) {render(
"""
this is
plain text
"""
    )}
  }

  test("Plain text can be nested in a tag") {
    expect(
"""
<html>
  this is
  plain text
</html>
"""
    ) {render(
"""
%html
  this is
  plain text
"""
    )}
  }

  test("Prefix a line with '\\' to force the line to be plain text") {
    expect(
"""
<html>
  %body is
  plain text
</html>
"""
    ) {render(
"""
%html
  \%body is
  plain text
"""
    )}
  }

  test("'%tag text' render start tag, text, and end tag on same line") {
    expect(
"""
<html>test</html>
"""
    ) {render(
"""
%html test
"""
    )}
  }

  test("nested tags are rendered indented") {
    expect(
"""
<html>
  <body>
    test
  </body>
</html>
"""
    ) {render(
"""
%html
  %body
    test
"""
    )}
  }

  test("'%tag>' trims the whitespace surrounding the tag'") {
    expect(
"""
<html><body>
    test
  </body></html>
"""
    ) {render(
"""
%html
  %body>
    test
"""
    )}
  }

  test("'%tag<' trims the whitespace wrapping nested content'") {
    expect(
"""
<html>
  <body>test</body>
</html>
"""
    ) {render(
"""
%html
  %body<
    test
"""
    )}
  }

  test("'%tag><' trims the whitespace surrounding the tag and wrapping nested content'") {
    expect(
"""
<html><body>test</body></html>
"""
    ) {render(
"""
%html
  %body><
    test
"""
    )}
  }

  test("'%tag<>' trims the whitespace surrounding the tag and wrapping nested content'") {
    expect(
"""
<html><body>test</body></html>
"""
    ) {render(
"""
%html
  %body<>
    test
"""
    )}
  }

  test("'= expression' renders a dynamic expression") {
    expect(
"""
<html>
  <body>
    10
  </body>
</html>
"""
    ) {render(
"""
%html
  %body
    = 5 + 5
"""
    )}
  }

  test("'= var' expressions can acess implicitly bound variables") {
    expect(
"""
<html>
  <body>
    Hiram
  </body>
</html>
"""
    ) {render(
"""
%html
  %body
    = context.name
"""
    )}
  }

  test("'= var' expressions can access imported variables") {
    expect(
"""
<html>
  <body>
    Hiram
  </body>
</html>
"""
    ) {render(
"""
%html
  %body
    = name
"""
    )}
  }

  test("'%tag= expression' render start tag, exression result, and end tag on same line") {
    expect(
"""
<html>
  <body>Hiram</body>
</html>
"""
    ) {render(
"""
%html
  %body= name
"""
    )}
  }


  test("'/ text' renders an html comment") {
    expect(
"""
<html>
  <body>
    <!--Test-->
  </body>
</html>
"""
    ) {render(
"""
%html
  %body
    /Test
"""
    )}
  }

  test("'/' can html comment a whole block of shaml") {
    expect(
"""
<html>
  <!--
    <body>
      Test
    </body>
  -->
</html>
"""
    ) {render(
"""
%html
  /
    %body
      Test
"""
    )}
  }

  test("'/[condition]' creates a conditional comment") {
    expect(
"""
<html>
  <!--[if IE]>
    <body>
      Test
    </body>
  <![endif]-->
</html>
"""
    ) {render(
"""
%html
  /[if IE]
    %body
      Test
"""
    )}
  }

  testRender(
"'-#' shaml comments ",
"""
%html
  -# this is a test
  -# more stuff
    %body
    will be hidden
  Test
""",
"""
<html>
  Test
</html>
""")

  test("loop constructs don't need {} ") {
    expect(
"""
<ol>
  <li>start</li>
  <li>Hi 1</li>
  <li>Hi 2</li>
  <li>Hi 3</li>
  <li>end</li>
</ol>
"""
    ) {render(
"""
%ol
  %li start
  - for( i <- 1 to 3 )
    - val message = "Hi "+i
    %li= message
  %li end
"""
    )}
  }

  testRender(
"'= expression' is not sanitized by default",
"""
= "I feel <strong>!"
""",
"""
I feel <strong>!
""")

  testRender(
"'&= expression' sanitizes the rendered expression",
"""
&= "I like cheese & crackers"
""",
"""
I like cheese &amp; crackers
""")

  testRender(
"'& text' santizes interpolated expressions",
"""
&I like #{"cheese & crackers"}
""",
"""
I like cheese &amp; crackers
""")

  testRender(
"'!= expression' does not santize the rendered expression",
"""
!= "I feel <strong>!"
""",
"""
I feel <strong>!
""")

  testRender(
"'! text' does not santize interpolated expressions",
"""
!I feel #{"<strong>"}!
""",
"""
I feel <strong>!
""")

  testRender(
"'-@ attribute' makes an attribute accessibe as variable",
"""
-@ attribute bean:Bean
The bean is #{bean.color}
""",
"""
The bean is red
""")

  testRender(
"'-@ import attribute' makes an attribute's members accessibe as variables",
"""
-@ import attribute bean:Bean
The bean is #{color}
""",
"""
The bean is red
""")

  testRender(
"'-@ attribute name:type = expression' can specify a default value if the named attribute is not set",
"""
-@ attribute doesnotexist:Bean = Bean("blue", 5)
The bean is #{doesnotexist.color}
""",
"""
The bean is blue
""")

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
  engine.workingDirectoryRoot = new File("target/test-data/"+(this.getClass.getName))

  def render(content:String): String = {

    engine.resourceLoader = new FileResourceLoader {
      override def load( uri: String ): String = content
      override def lastModified(uri:String): Long = 0
    }

    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = new DefaultRenderContext(out) {
      val name = "Hiram"
    }

    context.attributes += "context"-> context
    context.attributes += "bean"-> Bean("red", 10)

    val template = engine.loadTemporary("/org/fusesource/scalate/shaml/test.shaml", TemplateArg("context", context.getClass.getName, true))
    template.render(context)
    out.close
    buffer.toString
  }

}

case class Bean(color:String, size:Int)
