/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.scuery.support

class NthPseduoTest extends CssParserTestSupport {

  val cheese = <c:tr xmlns:c="http://apache.org/cheese"><blah/></c:tr>
  val a = <a href="http://scalate.github.io/scalate/" title="Scalate" hreflang="en-US">Awesomeness</a>

  val x1 = <li id="1" class="foo">one</li>
  val x2 = <li id="2" class="foo">two</li>
  val x3 = <li id="3" class="foo">three</li>

  val xml = <ul>
    {x1}
    {x2}
    {x3}
    </ul>

  assertMatches("li:nth-child(2n+1)", x1)
  assertNotMatches("li:nth-child(2n+1)", x2)
  assertMatches("li:nth-child(2n+1)", x3)

  assertMatches("li:nth-child(odd)", x1)
  assertNotMatches("li:nth-child(odd)", x2)
  assertMatches("li:nth-child(odd)", x3)

  assertNotMatches("li:nth-child(even)", x1)
  assertMatches("li:nth-child(even)", x2)
  assertNotMatches("li:nth-child(even)", x3)
}