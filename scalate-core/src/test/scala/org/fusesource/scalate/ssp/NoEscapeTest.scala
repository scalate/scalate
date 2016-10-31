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

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File

/**
 * @version $Revision : 1.1 $
 */
class NoEscapeTest extends TemplateTestSupport {

  test("disable markup escaping") {
    assertSspOutput("a = x > 5 && y < 3", """<% escapeMarkup = false %>
<% val foo = "x > 5 && y < 3" %>
a = ${foo}""")
  }

  test("using unescape function") {
    assertSspOutput("b = x > 5 && y < 3", """<% val foo = "x > 5 && y < 3" %>
b = ${unescape(foo)}""")
  }

}