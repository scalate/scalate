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

//import org.fusesource.scalate.scuery.Selector

class CssParserTest extends CssParserTestSupport {

  val cheese = <c:tr xmlns:c="http://apache.org/cheese"><blah/></c:tr>
  val a = <a href="http://scalate.fusesource.org/" title="Scalate" hreflang="en-US">Awesomeness</a>

  val xml = <table id="t1" class="people" summary="My Summary Notes">
              <tr id="tr1" class="personRow odd">
                <td class="person">Hey</td>
              </tr>
              { cheese }
              { a }
            </table>

  val tr1 = (xml \\ "tr")(0)
  val td1 = (xml \\ "td")(0)

  // simple stuff
  assertMatches("table", xml)
  assertMatches("table#t1", xml)
  assertMatches("#t1", xml)
  assertMatches(".people", xml)
  assertMatches("table.people", xml)

  assertMatches("*", td1)
  assertMatches("*.person", td1)
  assertMatches("*#tr1", tr1)
  assertMatches("#tr1", tr1)
  assertMatches(".personRow", tr1)
  assertMatches(".odd", tr1)

  // combinators
  assertMatches("tr > td", td1)
  assertMatches("tr td", td1)
  assertMatches("table td", td1)
  assertMatches("table tr td", td1)
  assertMatches("table tr .person", td1)
  assertMatches("table > tr > td", td1)
  assertMatches("table .person", td1)
  assertMatches("td.person", td1)
  assertMatches("tr .person", td1)
  assertMatches("tr > .person", td1)

  assertNotMatches("foo", td1)
  assertNotMatches(".foo", td1)
  assertNotMatches("#foo", td1)
  assertNotMatches("tr table td", td1)
  assertNotMatches("table tr tr td", td1)
  assertNotMatches("table table tr td", td1)
  assertNotMatches("table > td", td1)
  assertNotMatches("tr > table > td", td1)
  assertNotMatches("td > tr", td1)

  // namespaces
  assertMatches("*|*", cheese)
  assertMatches("*|*", tr1)

  assertMatches("*|tr", cheese)
  assertMatches("*|tr", tr1)

  assertNotMatches("|tr", cheese)
  assertMatches("|tr", tr1)

  assertMatches("c|tr", cheese)
  assertNotMatches("c|tr", tr1)

  // attributes
  assertMatches("table[summary]", xml)
  assertMatches("[summary]", xml)
  assertNotMatches("[summary]", tr1)

  assertMatches("[summary = \"My Summary Notes\"]", xml)
  assertNotMatches("[summary = \"NotMatch\"]", xml)

  assertMatches("[summary=\"My Summary Notes\"]", xml)

  // ~= matches whole words
  assertMatches("[summary ~= \"My\"]", xml)
  assertMatches("[summary ~= \"Summary\"]", xml)
  assertMatches("[summary ~= \"Notes\"]", xml)
  assertNotMatches("[summary ~= \"My Summary\"]", xml) // can only filter on whole words

  assertMatches("[summary~=\"My\"]", xml)

  // |=
  assertMatches("[hreflang |= \"en\"]", a)
  assertMatches("[hreflang |= \"en-US\"]", a)
  assertNotMatches("[hreflang |= \"de\"]", a)

  // TODO regex bug!!!
  // not sure yet why the parser fails to parse this!
  //assertMatches("[hreflang|=\"en\"]", a)
  //assertMatches("[hreflang|=en]", a)

  assertMatches("[hreflang |= en]", a)
  assertMatches("[hreflang |= en-US]", a)

  // ^=
  assertMatches("[summary ^= \"My\"]", xml)
  assertMatches("[summary ^= \"My S\"]", xml)
  assertNotMatches("[summary ^= \"T\"]", xml)

  assertMatches("[summary^=\"My\"]", xml)

  // $=
  assertMatches("[summary $= \"Notes\"]", xml)
  assertMatches("[summary $= \"Summary Notes\"]", xml)
  assertNotMatches("[summary $= \"Cheese\"]", xml)

  assertMatches("[summary$=\"Notes\"]", xml)

  // *=
  assertMatches("[summary *= \"Sum\"]", xml)
  assertMatches("[summary *= \"Summary N\"]", xml)
  assertNotMatches("[summary *= \"Cheese\"]", xml)

  assertMatches("[summary*=\"Sum\"]", xml)

  // :not
  assertMatches("td:not(.food)", td1)
  assertNotMatches("td:not(.person)", td1)

  // filtering using the pimped API on scala Node*
  assertFilter(".person", td1)
  assertFilter("table td", td1)
  assertFilter("table tr td", td1)
}
