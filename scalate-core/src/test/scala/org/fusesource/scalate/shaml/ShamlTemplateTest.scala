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

  test("'%tag' renders a tag") {
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
  
  ignore("'%tag/' renders a closed tag") {
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

  ignore("'%tag{:name -> \"value\"}' tag attributes can be specified using a ruby hash syntax") {
    expect(
"""
<html k1="v1" k2="v2"></html>
"""
    ) {render(
"""
%html{:k1->"v1", "k2"->"v2"}
"""
    )}
  }

  ignore("'%tag{:name -> \"value\"}' tag attributes using hash syntax can span multiple lines after the comma") {
    expect(
"""
<html>
  <body k1="v1" k2="v2"></body>
</html>

"""
    ) {render(
"""
%html
  %body{:k1->"v1",
"k2"->"v2"}
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

  ignore("'/' can html comment a whole block of shaml") {
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

  ignore("'/[condition]' creates a conditional comment") {
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
    
    val template = engine.loadTemporary("/test.shaml", TemplateArg("context", context.getClass.getName, true))
    template.render(context)
    out.close
    buffer.toString
  }

}
