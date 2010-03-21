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
 * Some tests for Scaml bugs reported
 */
@RunWith(classOf[JUnitRunner])
class ScamlBugTest extends ScamlTestSupport {

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

}