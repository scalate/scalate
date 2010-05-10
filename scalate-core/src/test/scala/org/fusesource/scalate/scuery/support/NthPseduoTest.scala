package org.fusesource.scalate.scuery.support

class NthPseduoTest extends CssParserTestSupport {

  val cheese = <c:tr xmlns:c="http://apache.org/cheese"><blah/></c:tr>
  val a = <a href="http://scalate.fusesource.org/" title="Scalate" hreflang="en-US">Awesomeness</a>

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