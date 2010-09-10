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

/**
 * Some tests for Scaml bugs reported
 */
class ScamlBugTest extends ScamlTestSupport {

  testRender("#155: Scaml :!plain filter breaks when used with multiple content lines",
"""
- val msg = "hello"
:!plain
  #{msg}, user
  The greeting is #{msg}.
""","""
hello, user
The greeting is hello.
""")

  testRender("#99: error if a comment containing just one space is used",
"-#\n-# \ntest\n/\n/ \ntest\n","""
test
<!--  -->
<!--  -->
test
""")

  testRender("#98: Error with a statement followed by an attribute declaration",
"""
- attributes("title") = "foo"
-@ import val title: String
= length
""","""
3
""")

  testRender("#77: attribute sanitized twice",
"""
- val amp = "&"
%div(attr1="#{amp}")
""","""
<div attr1="&amp;"></div>
""")

  testRender("#78: null class attribute not removed",
"""
%div(id={null} attr1={null} class={null})
""","""
<div></div>
""")

  testRender("#74: scaml id or class + dynamic attribute produces an error",
"""
%div.some(attr1={"value"})
%div#some(attr1={"value"})
""","""
<div class="some" attr1="value"></div>
<div id="some" attr1="value"></div>
""")

    testRender("SCALATE-44 test1",
"""
- if (name == "Hiram")
  - if (title == "MyPage")
    Worked!

  - else
    Failed

- else
  Failed
""","""
Worked!
""")


  testRender("SCALATE-45: creating a link with a title seems to remove whitespace from inside the title attribute",
"""
%a(href={1+2} title="foo bar")
""","""
<a href="3" title="foo bar"></a>
""")

  testRender("SCALATE-48: odd compile error when whitespace added to end of '-@ val: x: String '",
"""
-@ val label: String
%p #{label}
""","""
<p>Scalate</p>
""")

  testRender("SCALATE-49: using a #{foo} expression inside a HTML attribute causes strangeness",
"""
- var x="blue"
%div(class="line #{x}")
""","""
<div class="line blue"></div>
""")

  testRender("SCALATE-49: simple case",
"""
%pre(class="brush: html")<
  test
""","""
<pre class="brush: html">test</pre>
""")

  testRender("SCALATE-71: Internation characters",
"""
%div<
  1 한글 Hello &
%div<
  = "2 한글 Hello &"
%div<
  != "3 한글 Hello &"
%div<
  &= "4 한글 Hello &"
%div<
  ~~ "5 한글 Hello &"
%div<
  !~~ "6 한글 Hello &"
%div<
  &~~ "7 한글 Hello &"
""","""
<div>1 한글 Hello &</div>
<div>2 한글 Hello &amp;</div>
<div>3 한글 Hello &</div>
<div>4 한글 Hello &amp;</div>
<div>5 한글 Hello &amp;</div>
<div>6 한글 Hello &</div>
<div>7 한글 Hello &amp;</div>
""")

  testRender("SCALATE-71: Internation characters with escapeMarkup=false ",
"""
%div<
  1 한글 Hello &
%div<
  = "2 한글 Hello &"
%div<
  != "3 한글 Hello &"
%div<
  &= "4 한글 Hello &"
%div<
  ~~ "5 한글 Hello &"
%div<
  !~~ "6 한글 Hello &"
%div<
  &~~ "7 한글 Hello &"
""","""
<div>1 한글 Hello &</div>
<div>2 한글 Hello &</div>
<div>3 한글 Hello &</div>
<div>4 한글 Hello &amp;</div>
<div>5 한글 Hello &</div>
<div>6 한글 Hello &</div>
<div>7 한글 Hello &amp;</div>
""",
()=>{
  engine.escapeMarkup = false;
}, ()=>{
  engine.escapeMarkup = true;
})


  testRender("SCALATE-72: Spaces stripped in an attribute expression",
"""
%p(class={"a b"}) a b
""","""
<p class="a b">a b</p>
""")

}
