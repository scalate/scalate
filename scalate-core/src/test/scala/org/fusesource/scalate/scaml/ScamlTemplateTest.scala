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

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.fusesource.scalate._
import java.io.{StringWriter, PrintWriter, File}
/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class ScamlTemplateTest extends FunSuite {

  testRender("a '%tag' can have trailing spaces and nested content",
"""
%html  
  %body
""","""
<html>
  <body></body>
</html>
""")

  testRender("'%tag' renders a start and end tag",
"""
%html
""","""
<html></html>
""")

  testRender("'%tag/' renders a closed tag",
"""
%html/
""","""
<html/>
""")

  testRender("'%tag#i1' renders a tag with an id",
"""
%html#i1
""","""
<html id="i1"></html>
""")

  testRender("'%tag#i1#i2' last id specified wins",
"""
%html#i1#i2
""","""
<html id="i2"></html>
""")

  testRender("'%tag.c1' renders a tag with a class",
"""
%html.c1
""","""
<html class="c1"></html>
""")

  testRender("'%tag.c1.c2' renders a tag with multiple classes",
"""
%html.c1.c2
""","""
<html class="c1 c2"></html>
""")

  testRender("'.c1' if tag name is omitted, it defaults to div",
"""
.c1
""","""
<div class="c1"></div>
""")

  testRender("'%tag{:name => \"value\"}' tag attributes can be specified using a ruby hash syntax",
"""
%html{:k1=>"v1", "k2"=>"v2"}
""","""
<html k1="v1" k2="v2"></html>
""")

  testRender("The '%tag{:name => \"value\"}' attribute syntax can span multiple lines after the comma",
"""
%html
  %body{:k1=>"v1",
"k2"=>"v2"}
""","""
<html>
  <body k1="v1" k2="v2"></body>
</html>
""")

  testRender("'%tag(name=\"value\")' tag attributes can be specified html attribute syntax",
"""
%html(k1="v1" k2="v2")
""","""
<html k1="v1" k2="v2"></html>
""")

  testRender("The '%tag(name=\"value\")' attribute syntax can span multiple lines",
"""
%html
  %body(k1="v1"
k2="v2")
""","""
<html>
  <body k1="v1" k2="v2"></body>
</html>
""")

  testRender("Plain text is rendered as plain text",
"""
this is
plain text
""","""
this is
plain text
""")

  testRender("Plain text can be nested in a tag",
"""
%html
  this is
  plain text
""","""
<html>
  this is
  plain text
</html>
""")

  testRender("Prefix a line with '\\' to force the line to be plain text",
"""
%html
  \%body is
  plain text
""","""
<html>
  %body is
  plain text
</html>
""")

  testRender("'%tag text' render start tag, text, and end tag on same line",
"""
%html test
""","""
<html>test</html>
""")

  testRender("nested tags are rendered indented",
"""
%html
  %body
    test
""","""
<html>
  <body>
    test
  </body>
</html>
""")

  testRender("'%tag>' trims the whitespace surrounding the tag'",
"""
%html
  %body>
    test
""","""
<html><body>
    test
  </body></html>
""")

  testRender("'%tag<' trims the whitespace wrapping nested content'",
"""
%html
  %body<
    test
""","""
<html>
  <body>test</body>
</html>
""")

  testRender("'%tag><' trims the whitespace surrounding the tag and wrapping nested content'",
"""
%html
  %body><
    test
""","""
<html><body>test</body></html>
""")

  testRender("'%tag<>' trims the whitespace surrounding the tag and wrapping nested content'",
"""
%html
  %body<>
    test
""","""
<html><body>test</body></html>
""")

  testRender("'= expression' renders a dynamic expression",
"""
%html
  %body
    = 5 + 5
""","""
<html>
  <body>
    10
  </body>
</html>
""")

  testRender("'= var' expressions can acess implicitly bound variables",
"""
%html
  %body
    = context.name
""","""
<html>
  <body>
    Hiram
  </body>
</html>
""")

  testRender("'= var' expressions can access imported variables",
"""
%html
  %body
    = name
""","""
<html>
  <body>
    Hiram
  </body>
</html>
""")

  testRender("'%tag= expression' render start tag, exression result, and end tag on same line",
"""
%html
  %body= name
""","""
<html>
  <body>Hiram</body>
</html>
""")

  testRender("'/ text' renders an html comment",
"""
%html
  %body
    /Test
""","""
<html>
  <body>
    <!--Test-->
  </body>
</html>
""")

  testRender("'/' can html comment a whole block of scaml",
"""
%html
  /
    %body
      Test
""","""
<html>
  <!--
    <body>
      Test
    </body>
  -->
</html>
""")

  testRender("'/[condition]' creates a conditional comment",
"""
%html
  /[if IE]
    %body
      Test
""","""
<html>
  <!--[if IE]>
    <body>
      Test
    </body>
  <![endif]-->
</html>
""")

  testRender("'-#' scaml comments ",
"""
%html
  -# this is a test
  -# more stuff
    %body
    will be hidden
  Test
""","""
<html>
  Test
</html>
""")

  testRender("loop constructs don't need {} ",
"""
%ol
  %li start
  - for( i <- 1 to 3 )
    - val message = "Hi "+i
    %li= message
  %li end
""","""
<ol>
  <li>start</li>
  <li>Hi 1</li>
  <li>Hi 2</li>
  <li>Hi 3</li>
  <li>end</li>
</ol>
""")

  testRender("'= expression' is not sanitized by default",
"""
= "I feel <strong>!"
""","""
I feel <strong>!
""")

  testRender("'&= expression' sanitizes the rendered expression",
"""
&= "I like cheese & crackers"
""","""
I like cheese &amp; crackers
""")

  testRender("'& text' santizes interpolated expressions",
"""
& I like #{"cheese & crackers"}
""","""
I like cheese &amp; crackers
""")

  testRender("'!= expression' does not santize the rendered expression",
"""
!= "I feel <strong>!"
""","""
I feel <strong>!
""")

  testRender("'! text' does not santize interpolated expressions",
"""
! I feel #{"<strong>"}!
""","""
I feel <strong>!
""")

  testRender("'-@ val' makes an attribute accessibe as variable",
"""
-@ val bean:Bean
The bean is #{bean.color}
""","""
The bean is red
""")

  testRender("'-@ import val' makes an attribute's members accessibe as variables",
"""
-@ import val bean:Bean
The bean is #{color}
""","""
The bean is red
""")

  testRender("'-@ val name:type = expression' can specify a default value if the named attribute is not set",
"""
-@ val doesnotexist:Bean = Bean("blue", 5)
The bean is #{doesnotexist.color}
""","""
The bean is blue
""")

  testRender("'-@ val' can be used in nested tags",
"""
%html
  test
  -@ val bean:Bean
  The bean is #{bean.color}
""","""
<html>
  test
  The bean is red
</html>
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

    val template = engine.loadTemporary("/org/fusesource/scalate/scaml/test.scaml", TemplateArg("context", context.getClass.getName, true))
    template.render(context)
    out.close
    buffer.toString
  }  
}

case class Bean(color:String, size:Int)
