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
package org.fusesource.scalate
package ssp


class WhitespaceTest extends TemplateTestSupport {

  showOutput = true

  test("regular ssp directive") {
    assertSspOutput("""
  person: James
  person: Hiram
""", """
<% val people = List("James", "Hiram") %>
<% for (p <- people) { %>
  person: ${p}
<% } %>
""")
  }

  test("velocity style ssp directive") {
    assertSspOutput("""
  person: James
  person: Hiram
""", """
<% val people = List("James", "Hiram") %>
#for(p <- people)
  person: ${p}
#end
""")
  }

  test("ssp ${...} expression whitespace") {
    assertSspOutput("""
  Copyright 2010 MyCompany
""", """
<% val year = "2010" %>
  Copyright ${year} MyCompany
""")
  }

  test("ssp <%=...%> expression whitespace") {
    assertSspOutput("""
  Copyright 2010 MyCompany
""", """
<% val year = "2010" %>
  Copyright <%=year%> MyCompany
""")
  }

  test("ssp <%=...%> expression whitespace 2") {
    assertSspOutput("""
  <tr class="featured">
""", """
  <tr<% if(true) { %> class="featured"<% } %>>
""")
  }

  test("ssp <%=...%> expression whitespace 3") {
    assertSspOutput("""
  Hello World!""", """
  Hello <% if(true) { %>World!<% } %>
""")
  }

  test("ssp <%=...%> expression whitespace 4") {
    assertSspOutput("""
  Hello World!
""", """
  Hello <% if(true) { %>World!<% } +%>
""")
  }
}