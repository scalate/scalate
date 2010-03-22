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

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class ScamlTemplateTest extends ScamlTestSupport {

  /////////////////////////////////////////////////////////////////////
  //
  // Filters
  //
  /////////////////////////////////////////////////////////////////////

  testRender("You can use `:` to use filters",
"""
%html
  %p
    :plain
          Indentation levels are not enforced in filters.
        #{Interpolation} is disabled by default
      Last line
""","""
<html>
  <p>
        Indentation levels are not enforced in filters.
      #{Interpolation} is disabled by default
    Last line
  </p>
</html>
""")

  testRender("Use the `~` filter flag to preserve white space",
"""
%html
  %p<
    :~plain
          Indentation levels are not enforced in filters.
        #{Interpolation} is disabled by default
      Last line
""","""
<html>
  <p>    Indentation levels are not enforced in filters.&#x000A;  #{Interpolation} is disabled by default&#x000A;Last line</p>
</html>
""")

  testRender("Use the `&` filter flag to enable sanitizing interpolation",
"""
%html
  %p
    :&plain
      I like #{ "<strong>" } cheese
""","""
<html>
  <p>
    I like &lt;strong&gt; cheese
  </p>
</html>
""")

  testRender("Use the `!` filter flag to enable non-sanitizing interpolation",
"""
%html
  %p
    :!plain
      I like #{ "<strong>" } cheese #{ "</strong>" }
""","""
<html>
  <p>
    I like <strong> cheese </strong>
  </p>
</html>
""")

  testRender(":javascript filter can be used to safely insert javascript",
"""
%html
  %head
    :javascript
      alert("Hello");
""","""
<html>
  <head>
    <script type='text/javascript'>
      //<![CDATA[
        alert("Hello");
      //]]>
    </script>
  </head>
</html>
""")

  testRender("filters can be chained",
"""
%pre
  :escaped :javascript
    alert("Hello");
""","""
<pre>
  &lt;script type='text/javascript'&gt;
    //&lt;![CDATA[
      alert(&quot;Hello&quot;);
    //]]&gt;
  &lt;/script&gt;
</pre>
""")

  testRender("The markdown filter",
"""
%p
  :markdown
    Markdown
    =======
        
    Hello, *World*
""","""
<p>
  <h1>Markdown</h1>

  <p>Hello, <em>World</em></p>
</p>
""")

  testRender("The `&` flag enables sanitized interpolations.",
"""
- var flavor = "<raspberry/>"
#content
  :&markdown
    I *really* prefer #{flavor} jam.
""","""
<div id="content">
  <p>I <em>really</em> prefer &lt;raspberry/&gt; jam.</p>
</div>
""")

  testRender("The `!` flag enables non-sanitized interpolations.",
"""
- var flavor = "<raspberry/>"
#content
  :!markdown
    I *really* prefer #{flavor} jam.
""","""
<div id="content">
  <p>I <em>really</em> prefer <raspberry/> jam.</p>
</div>
""")


  /////////////////////////////////////////////////////////////////////
  //
  // Plain Text
  //
  /////////////////////////////////////////////////////////////////////

  testRender("Any Scaml line that's not interpreted as something else is taken to be plain text, and passed through unmodified. ",
"""
%gee
  %whiz
    Wow this is cool!
""","""
<gee>
  <whiz>
    Wow this is cool!
  </whiz>
</gee>
""")

  testRender("HTML tags are passed through unmodified as well.",
"""
%p
  <div id="blah">Blah!</div>
""","""
<p>
  <div id="blah">Blah!</div>
</p>
""")

  testRender("backslash character escapes the first character of a line, allowing use of otherwise interpreted characters as plain text.",
"""
%title
  = title
  \= title
""","""
<title>
  MyPage
  = title
</title>
""")


  /////////////////////////////////////////////////////////////////////
  //
  // HTML Elements
  //
  /////////////////////////////////////////////////////////////////////


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


  /////////////////////////////////////////////////////////////////////
  //
  // HTML Elements : Attributes
  //
  /////////////////////////////////////////////////////////////////////

  testRender("Brackets represent a Scala hash that is used for specifying the attributes of an element.",
"""
%html{:xmlns => "http://www.w3.org/1999/xhtml", "xml:lang" => "en", :lang => "en"}
""","""
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"></html>
""")

  testRender("Attribute hashes can also be stretched out over multiple lines to accommodate many attributes.",
"""
%script{:type => "text/javascript",
            :src  => "javascripts/script"}
""","""
<script type="text/javascript" src="javascripts/script"></script>
""")


  testRender("Scaml also supports a terser, less Scala-specific attribute syntax based on HTML's attributes.",
"""
%html(xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en")
""","""
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"></html>
""")

  testRender("Scala variables can be used by omitting the quotes.",
"""
%a(title=title href=href) Stuff
""","""
<a title="MyPage" href="http://scalate.fusesource.org">Stuff</a>
""")

  testRender("You can use both syntaxes together.",
"""
%a(title="Hello"){:href => "http://scalate.fusesource.org"} Stuff
""","""
<a title="Hello" href="http://scalate.fusesource.org">Stuff</a>
""")

  testRender("HTML-style attributes can be stretched across multiple lines just like hash-style attributes",
"""
%script(type="text/javascript"
     src="javascripts/script")
""","""
<script type="text/javascript" src="javascripts/script"></script>
""")

  testRender("Html Attributes can use complex scala expressions",
"""
%div(count={3+4})
""","""
<div count="7"></div>
""")

  testRender("Html Attributes can use simple scala expressions",
"""
%div(count="#{3+4}")
""","""
<div count="7"></div>
""")

  testRender("Html Attributes can use scala variables",
"""
- val count = 5
%div(count=count)
""","""
<div count="5"></div>
""")

  testRender("Hash Attributes can use simple scala expressions",
"""
- val count = 5
%div{:count=>count}
""","""
<div count="5"></div>
""")

  testRender("Hash Attributes can use complex scala expressions",
"""
%div{:count=>{3+4}}
""","""
<div count="7"></div>
""")


  testRender("Enabled boolean attribute in hash style",
"""
%input{:selected => true}
""","""
<input selected="selected"/>
""")

  testRender("Disabled boolean attribute in hash style",
"""
%input{:selected => false}
""","""
<input/>
""")

  testRender("Enabled boolean attribute in html style",
"""
%input(selected=true)
""","""
<input selected="selected"/>
""")

  testRender("Disabled boolean attribute in html style",
"""
%input(selected=false)
""","""
<input/>
""")

  testRender("Enabled boolean attribute in html style using shorthand",
"""
%input(selected)
""","""
<input selected="selected"/>
""")

  /////////////////////////////////////////////////////////////////////
  //
  // HTML Elements : Class and ID: `.` and `#`
  //
  /////////////////////////////////////////////////////////////////////

  testRender("`.` and `#` are used as shortcuts to specify the `class` and `id` attributes of an element",
"""
%div#things
  %span#rice Chicken Fried
  %p.beans{ :food => "true" } The magical fruit
  %h1.class.otherclass#id La La La
""","""
<div id="things">
  <span id="rice">Chicken Fried</span>
  <p class="beans" food="true">The magical fruit</p>
  <h1 id="id" class="class otherclass">La La La</h1>
</div>
""")

  testRender("Another `.` and `#` example",
"""
#content
  .articles
    .article.title Doogie Howser Comes Out
    .article.date 2006-11-05
    .article.entry
      Neil Patrick Harris would like to dispel any rumors that he is straight
""","""
<div id="content">
  <div class="articles">
    <div class="article title">Doogie Howser Comes Out</div>
    <div class="article date">2006-11-05</div>
    <div class="article entry">
      Neil Patrick Harris would like to dispel any rumors that he is straight
    </div>
  </div>
</div>
""")


  // Edge cases

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

  testRender("Any css class/name can be used.",
"""
.my-class
._whacky_1
""","""
<div class="my-class"></div>
<div class="_whacky_1"></div>
""")

  /////////////////////////////////////////////////////////////////////
  //
  // HTML Elements : Implicit Div Elements
  //
  /////////////////////////////////////////////////////////////////////

  testRender("If you only define a class and/or id using `.` or `#`, a div is automatically used.",
"""
#collection
  .item
    .description What a cool item!
""","""
<div id="collection">
  <div class="item">
    <div class="description">What a cool item!</div>
  </div>
</div>
""")

  
  /////////////////////////////////////////////////////////////////////
  //
  // HTML Elements : Self-Closing Tags : `/`
  //
  /////////////////////////////////////////////////////////////////////


  testRender("The forward slash character, when placed at the end of a tag definition, causes the tag to be self-closed.",
"""
%br/
%meta{"http-equiv" => "Content-Type", :content => "text/html"}/
""","""
<br/>
<meta http-equiv="Content-Type" content="text/html"/>
""")

  testRender("`meta`, `img`, `link`, `script`, `br`, and `hr` tags are closed by default.",
"""
%br/
%meta{"http-equiv" => "Content-Type", :content => "text/html"}/
""","""
<br/>
<meta http-equiv="Content-Type" content="text/html"/>
""")
  


  /////////////////////////////////////////////////////////////////////
  //
  // HTML Elements : Whitespace Removal: `>` and `<`
  //
  /////////////////////////////////////////////////////////////////////
  
  testRender("'`<` will remove all whitespace immediately within a tag.",
"""
%blockquote<
  %div
    Foo!
""","""
<blockquote><div>
  Foo!
</div></blockquote>
""")

  testRender("`>` will remove all whitespace surrounding a tag",
"""
%img/
%img>/
%img/
""","""
<img/><img/><img/>
""")

  testRender("`<` will remove all whitespace surrounding a rendered expression",
"""
%p<= "Foo\nBar"
""","""
<p>Foo
Bar</p>
""")


  testRender("'%tag><' trims the whitespace surrounding the tag and wrapping nested content'",
"""
%img/
%pre><
  foo
  bar
%img/
""","""
<img/><pre>foo
bar</pre><img/>
""")

  testRender("'%tag<>' trims the whitespace surrounding the tag and wrapping nested content'",
"""
%img/
%pre><
  foo
  bar
%img/
""","""
<img/><pre>foo
bar</pre><img/>
""")


  /////////////////////////////////////////////////////////////////////
  //
  // Doctype: !!!
  //
  /////////////////////////////////////////////////////////////////////
  testRender("you can have a document type or XML prolog generated automatically by including the characters !!!",
"""
!!! XML
!!!
%html
  %head
    %title Myspace
  %body
    %h1 I am the international space station
    %p Sign my guestbook
""","""
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>Myspace</title>
  </head>
  <body>
    <h1>I am the international space station</h1>
    <p>Sign my guestbook</p>
  </body>
</html>
""")

  /////////////////////////////////////////////////////////////////////
  //
  // Comments
  //
  /////////////////////////////////////////////////////////////////////
  

  testRender("The `/`, when placed at the beginning of a line, wraps all text after it in an HTML comment",
"""
%peanutbutterjelly
  / This is the peanutbutterjelly element
  I like sandwiches!
""","""
<peanutbutterjelly>
  <!-- This is the peanutbutterjelly element -->
  I like sandwiches!
</peanutbutterjelly>
""")

  testRender("The `/` can also wrap indented sections of code.",
"""
/
  %p This doesn't render...
  %div
    %h1 Because it's commented out!
""","""
<!--
  <p>This doesn't render...</p>
  <div>
    <h1>Because it's commented out!</h1>
  </div>
-->
""")

  testRender("You can also use Internet Explorer conditional comments by enclosing the condition in square brackets",
"""
/[if IE]
  %a{ :href => "http://www.mozilla.com/en-US/firefox/" }
    %h1 Get Firefox
""","""
<!--[if IE]>
  <a href="http://www.mozilla.com/en-US/firefox/">
    <h1>Get Firefox</h1>
  </a>
<![endif]-->
""")

  testRender("`-#` signifies a silent comment.",
"""
%p foo
-# This is a comment
%p bar
""","""
<p>foo</p>
<p>bar</p>
""")

  testRender("You can also nest text beneath a `-#`",
"""
%p foo
-#
  This won't be displayed
    Nor will this
%p bar
""","""
<p>foo</p>
<p>bar</p>
""")


  /////////////////////////////////////////////////////////////////////
  //
  // Scala Evaluation: Inserting Scala: `=`
  //
  /////////////////////////////////////////////////////////////////////

  testRender("`=` is followed by Scala code. This code is evaluated and the output is inserted into the document.",
"""
%p
  = List("hi", "there", "reader!").mkString(" ")
  = "yo"
""","""
<p>
  hi there reader!
  yo
</p>
""")


  test("If the ScamlOptions.escape_html option is set, `=` will sanitize any HTML-sensitive characters generated by the script.") {
    expect("""&lt;script&gt;alert(&quot;I'm evil!&quot;);&lt;/script&gt;""") {
      ScamlOptions.escape_html = true
      try {
        render("""
= """+"\"\"\""+"""<script>alert("I'm evil!");</script>"""+"\"\"\""+"""
""").trim
      } finally {
        ScamlOptions.escape_html = false
      }
    }
  }


  testRender("`=` can also be used at the end of a tag to insert Scala code within that tag.",
"""
%p= "hello"
""","""
<p>hello</p>
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

  testRender("= on a NodeSeq is rendered unsanitized",
"""
-@ val bean:Bean
= bean.link
""","""
<a href="#size-10">red</a>
""")

  testRender("!= a NodeSeq is rendered unsanitized",
"""
-@ val bean:Bean
!= bean.link
""","""
<a href="#size-10">red</a>
""")
  testRender("&= a NodeSeq is rendered sanitized",
"""
-@ val bean:Bean
&= bean.link
""","""
&lt;a href=&quot;#size-10&quot;&gt;red&lt;/a&gt;
""")

  /////////////////////////////////////////////////////////////////////
  //
  // Scala Evaluation: Running Scala: `-`
  //
  /////////////////////////////////////////////////////////////////////


  testRender("`-` is followed by Scala code.  The code is evaluated but *not* inserted into the document.",
"""
- var foo = "hello"
- foo += " there"
- foo += " you!"
%p= foo
""","""
<p>hello there you!</p>
""")
  
  testRender("`-` is followed by an indented Scala code block.  The code is evaluated but *not* inserted into the document.",
"""
-
  var foo = "hello"
      // note: you can use creative indentation in the block
      foo += " there"
  foo += " you!"
%p= foo
""","""
<p>hello there you!</p>
""")

  /////////////////////////////////////////////////////////////////////
  //
  // Scala Evaluation: Scala Blocks
  //
  /////////////////////////////////////////////////////////////////////

  testRender("Nest block for case statements",
"""
%p
  - 2 match
    - case 1 =>
      = "one"
    - case 2 =>
      = "two"
    - case 3 =>
      = "three"
""","""
<p>
  two
</p>
""")


  testRender("Nest block for a `for` loop",
"""
- for(i <- 42 to 46)
  %p= i
%p See, I can count!
""","""
<p>42</p>
<p>43</p>
<p>44</p>
<p>45</p>
<p>46</p>
<p>See, I can count!</p>
""")
  
  testRender("Passing partial funtions.",
"""
%p
  = List(1,2,3).foldLeft("result: ")
    - (a,x)=>
      - a+x
""","""
<p>
  result: 123
</p>
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

  testRender("if / else constructs",
"""
- if ( 1==2 )
  %p alternate reality
- else
  %p still on earth
""","""
<p>still on earth</p>
""")

  testRender("try / catch constructs",
"""
- try
  %p in try
  - throw new IllegalStateException()
- catch
  - case e:IllegalStateException =>
    %p got the expected error
  - case e:Exception =>
    %p some odd error occured
""","""
<p>in try</p>
<p>got the expected error</p>
""")


  /////////////////////////////////////////////////////////////////////
  //
  // Scala Interpolation: `#{}`
  //
  /////////////////////////////////////////////////////////////////////

  testRender("Scala code can be interpolated within plain text using `#{}`",
"""
%p This is #{quality} cake!
""","""
<p>This is scrumptious cake!</p>
""")

  testRender("Backslashes can be used to escape `#{` strings, but they don't act as escapes anywhere else in the string.",
"""
%p
  A slash make a difference here: \#{name} is set to: \\#{name}
  But is ignored for: \# or \\
""","""
<p>
  A slash make a difference here: #{name} is set to: \Hiram
  But is ignored for: \# or \\
</p>
""")


  /////////////////////////////////////////////////////////////////////
  //
  // Escaping HTML: `&=`
  //
  /////////////////////////////////////////////////////////////////////


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

  testRender("'= expression' is not sanitized by default",
"""
= "I feel <strong>!"
""","""
I feel <strong>!
""")

  /////////////////////////////////////////////////////////////////////
  //
  //  Unescaping HTML: `!=`
  //
  /////////////////////////////////////////////////////////////////////

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

  /////////////////////////////////////////////////////////////////////
  //
  // Preserving White Space
  //
  /////////////////////////////////////////////////////////////////////
  testRender("Generated content in dynamic expression are properly indented.",
"""
%html
  %p
    = "line1\nline2\nline3"
""","""
<html>
  <p>
    line1
    line2
    line3
  </p>
</html>
""")
  
  testRender("`~` preserves white space",
"""
%html
  %p
    ~ "line1\nline2\nline3"
""","""
<html>
  <p>
    line1&#x000A;line2&#x000A;line3
  </p>
</html>
""")

  testRender("`&~` preserves and sanitizes",
"""
&~ "<tag>\n</tag>"
""","""
&lt;tag&gt;&#x000A;&lt;/tag&gt;
""")

  testRender("`!~` preserves and does not sanitize",
"""
!~ "<tag>\n</tag>"
""","""
<tag>&#x000A;</tag>
""")

  testRender("`~~` ugly preserves white space. values are not indented at all.",
"""
%html
  %p
    ~~ "line1\nline2\nline3"
""","""
<html>
  <p>
line1
line2
line3
  </p>
</html>
""")

  /////////////////////////////////////////////////////////////////////
  //
  // Indent Handling..
  // You should enable white space viewing in your editor before touching this
  // section.
  //
  /////////////////////////////////////////////////////////////////////
  testRender("Scaml eats empty lines",
"""
%ul
  %li item 1

  %ul
    %li item 1

    %li item 2

  %li item 2

%p test
""","""
<ul>
  <li>item 1</li>
  <ul>
    <li>item 1</li>
    <li>item 2</li>
  </ul>
  <li>item 2</li>
</ul>
<p>test</p>
""")

  testRender("Scaml does not eat empty lines in a filter",
"""
:plain
  item 1

  item 2
%p test
""","""
item 1

item 2
<p>test</p>
""")
}

case class Bean(color:String, size:Int) {

  def link = {
    <a href={"#size-"+size}>{color}</a>
  }

}
