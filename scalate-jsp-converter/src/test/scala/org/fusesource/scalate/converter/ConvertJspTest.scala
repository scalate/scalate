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
package org.fusesource.scalate.converter

import _root_.org.junit.runner.RunWith
import _root_.org.scalatestplus.junit.JUnitRunner

import _root_.org.fusesource.scalate._
import org.fusesource.scalate.util.Log
import org.scalatest.funsuite.AnyFunSuite

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ConvertJspTest extends AnyFunSuite with Log {

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
    """blah <c:url value='/foo/${x}/bar/${y}'/> blah""",
    """blah ${uri("/foo/" + x + "/bar/" + y)} blah""")

  assertConvert(
    """<a href="<c:url value='/foo'/>">body</a>""",
    """<a href="${uri("/foo")}">body</a>""")

  assertConvert(
    """something <c:out value="${foo}"/> or other""",
    """something ${foo} or other""")

  assertConvert(
    """something <c:out value="${foo}" escapeXml="true"/> or other""",
    """something ${escape(foo)} or other""")

  assertConvert(
    """something <c:out value="${foo}" escapeXml="false"/> or other""",
    """something ${unescape(foo)} or other""")

  assertConvert(
    """something <c:out value="${foo}" escapeXml="x"/> or other""",
    """something ${value(foo, x)} or other""")

  assertConvert(
    """foo <c:if test='${foo}'> a <c:if test='${bar}'> b </c:if> c </c:if> whatnot""",
    """foo #if(foo) a #if(bar) b #end c #end whatnot""")

  assertConvert(
    """foo <c:set var="x" value='${foo}'/> whatnot""",
    """foo #{ var x = foo }# whatnot""")

  assertConvert(
    """foo <c:if test="${it.language eq 'Cheese'}"> bar </c:if> whatnot""",
    """foo #if(it.getLanguage == "Cheese") bar #end whatnot""")

  assertConvert(
    """foo <c:if test='${foo}'> bar </c:if> whatnot""",
    """foo #if(foo) bar #end whatnot""")

  assertConvert(
    """
foo
<c:if test="${x.y == 5}">
  bar
</c:if>
whatnot""",
    """
foo
#if(x.getY == 5)
  bar
#end
whatnot""")

  assertConvert(
    """
foo
<c:forEach var="foo" items="${something.whatnot}">
 blah ${foo.bar}
</c:forEach>
whatnot""",
    """
foo
#for(foo <- something.getWhatnot)
 blah ${foo.getBar}
#end
whatnot""")

  assertConvert(
    """
foo
<c:forEach var="i" begin="1" end="10">
 blah ${i}
</c:forEach>
whatnot""",
    """
foo
#for(i <- 1.to(10))
 blah ${i}
#end
whatnot""")

  assertConvert(
    """
foo
<c:forEach var="i" begin="1" end="10" step="3">
 blah ${i}
</c:forEach>
whatnot""",
    """
foo
#for(i <- 1.to(10, 3))
 blah ${i}
#end
whatnot""")

  assertConvert(
    """
foo
<c:choose>
<c:when test="${x == 5}">
five
</c:when>
<c:when test="${x == 6}">
six
</c:when>
<c:otherwise>
default
</c:otherwise>
</c:choose>
whatnot""",
    """
foo

#if(x == 5)
five

#elseif(x == 6)
six

#else
default

#end
whatnot""")

  def assertJustText(jsp: String): String = {
    val result = convert(jsp)
    assertResult(jsp, "converting JSP: " + jsp) { result }
    result

  }

  def assertConvert(jsp: String, ssp: String): String = {
    val result = convert(jsp)
    assertResult(ssp, "converting JSP: " + jsp) { result }
    result
  }

  def convert(jsp: String): String = {
    log.info("Converting JSP: " + jsp)

    val converter = new JspConverter
    val result = converter.convert(jsp)

    log.info(" => " + result)

    result
  }

}
