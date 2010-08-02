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

package org.fusesource.scalate.converter


import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.FunSuite
import _root_.java.io.File
import _root_.org.fusesource.scalate._

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ConvertJspTest extends FunSuite {

  assertJustText("<foo/>")
  assertJustText("text <foo/> text")
  assertJustText("<curl value='/foo'/>")

  assertConvert(
    """<c:url value='/foo'/>""",
    """${uri("/foo")}""")

  assertConvert(
    """blah <c:url value='/foo'/> blah""",
    """blah ${uri("/foo")} blah""")


  assertConvert(
    """<a href="<c:url value='/foo'/>">body</a>""",
    """<a href="${uri("/foo")}">body</a>""")

  if (false) {
    // TODO need to parse expressions in attribute values

  assertConvert(
    """foo <c:if test='${foo}'> bar </c:if> whatnot""",
    """foo #if(foo) bar #end whatnot""")

  assertConvert(
    """
foo
<c:if test="${x == 5}">
  bar
</c:if>
whatnot""",
    """
foo
#if(x == 5)
  bar
#end
whatnot""")

  }


  def assertJustText(jsp: String): String = {
    val result = convert(jsp)
    expect(jsp, "converting JSP: " + jsp){result}
    result

  }

  def assertConvert(jsp: String, ssp: String): String = {
    val result = convert(jsp)
    expect(ssp, "converting JSP: " + jsp){result}
    result
  }

  def convert(jsp: String): String = {
    println("Converting JSP: " + jsp)

    val converter = new JspConverter
    val result = converter.convert(jsp)

    println(" => " + result)
    println
    result
  }

  def assertParse(jsp: String, ssp: String): Unit = {
    val parser = new JspParser
    val result = parser.parsePage(jsp)

    println(jsp + " => " + result)
  }
}
