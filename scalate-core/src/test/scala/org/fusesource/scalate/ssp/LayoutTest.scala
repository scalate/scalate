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
package org.fusesource.scalate.ssp

import org.fusesource.scalate.{ TemplateSource, TemplateTestSupport }

/**
 * @version $Revision : 1.1 $
 */
class LayoutTest extends TemplateTestSupport {

  test("layout with no param") {
    val source = TemplateSource.fromText("sample.ssp", """
#do( layout("layout.ssp") )
 location: <b>London</b>
#end
""")
    assertOutputContains(source, "<body>", "location:", "London", "</body>")
  }

  test("layout with empty Map") {
    val source = TemplateSource.fromText("sample3.ssp", """
<% layout("layout.ssp", Map()) { %>
 location: <b>London</b>
<% } %>
""")
    assertOutputContains(source, "<body>", "location:", "London", "</body>")
  }

  test("layout with Map") {
    val source = TemplateSource.fromText("sample4.ssp", """
<% layout("layout.ssp", Map("foo" -> 1)) { %>
 location: <b>London</b>
<% } %>
""")
    assertOutputContains(source, "<body>", "location:", "London", "</body>")
  }

}