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

package org.fusesource.scalate.sample

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.{FunSuite}

import _root_.org.fusesource.scalate.test._

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class SampleTest extends FunSuite with WebServerMixin with WebDriverMixin {

  override protected def beforeAll(configMap: Map[String, Any]) = {
    System.setProperty("scalate.mode", "development")
    super.beforeAll(configMap)
  }

  testPageContains("ssp/renderObject.ssp", "Strachan")

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

  testPageContains("mustache", "Scala", "Great", "Java", "Crufty")
  testPageContains("mustache/errors/templateCompileError.mustache", "Missing", "'{{/items}}'", "near line 8")

  testPageContains("sampleServlet", "The foo is: Foo(")

  testPageContains("ssp/capture.ssp", "Hello James", "Sample 2", "Hello James")
  testPageContains("ssp/customLayout.ssp", "layout header goes here...", "Custom page", "This is some text", "layout footer goes here...")
  testPageContains("ssp/defaultAttribute.ssp", "James")
  testPageContains("ssp/exampleIncludes.ssp", "included from /includes/something.jsp",
    "included from /ssp/child/foo.ssp", "included from /includes/something.jsp", "Finished including!")

  testPageContains("ssp/errors/badAttributeType.ssp",
    "error: not found: type StringF",
    "in /ssp/errors/badAttributeType.ssp near line 3 col 13")
  
  testPageContains("ssp/errors/badExpression.ssp",
    "error: not found: value nameX",
    "in /ssp/errors/badExpression.ssp near line 5 col 13")

  testPageContains("ssp/errors/missingAttribute.ssp", "The value for 'name' was not set")
  testPageContains("ssp/errors/missingInclude.ssp", "Could not load resource")
  testPageContains("ssp/errors/missingView.ssp", "No 'index' view template could be found for model object")

  testPageContains("ssp/ifExpression.ssp", "x = 1", "x is 1", "x = 2", "x is not 1")
  testPageContains("ssp/ifExpression2.ssp", "x = 1", "x is 1", "x = 2", "x is not 1")
  testPageContains("ssp/includeServlet.ssp", "Scalate Start", "Hello World!", "Scalate End")
  testPageContains("ssp/implicitParamTest.ssp", "encoded '/somePath'", "encoded2 '/anotherPath'", "encoded3 '/path3'")
  testPageContains("ssp/locale.ssp", "22.0 / 7 = 3.143")
  testPageContains("ssp/matchExpression.ssp", "i = 1", "i is 1", "i = 2", "i is 2", "i = 3", "i is something")

  testPage("ssp/noLayout.ssp") {
    pageContains("No Layout", "This page has no separate layout")
    pageNotContains("stylesheet", "text/css", "style.css")
  }

//  testPageContains("ssp/renderObject.ssp", "Strachan")
  testPageContains("ssp/optionTest.ssp", "no bar", "no foo")
  testPageContains("ssp/optionTest2.ssp", "no foo")
  testPageContains("ssp/renderCaseClass.ssp", "Strachan", "<hr/>", "Chirino")
  testPageContains("ssp/renderCollection.ssp", "Strachan", "<hr/>", "Chirino")
  testPageContains("ssp/renderCollection2.ssp", "Strachan", "Person 2", "Chirino")
  testPageContains("ssp/renderCollection3.ssp", "Strachan", "Dude 2", "Chirino")
  testPageContains("ssp/renderTemplate.ssp", "James", "London", "Hiram", "Tampa", "Matt", "No Location", "Paul", "USA")
  testPageContains("ssp/sampleTag.ssp",  "Wrapped body", "hey James this is some body text!", "End of wrapped body")
  testPageContains("ssp/sampleTag2.ssp", "Wrapped body", "hey James this is some body text!", "End of wrapped body")
  testPageContains("ssp/sampleTag3.ssp", "Wrapped body", "hey Hiram this is some body text!", "End of wrapped body")
  testPageContains("ssp/simple.ssp", "1 + 2 = 3")
  testPageContains("ssp/snippet.ssp", "mmm I like beer")
  testPageContains("ssp/velocity/sampleTag.ssp",  "Wrapped body", "hey James this is some body text!", "End of wrapped body")

  testPageContains("bad", "error: not found: value unknown")
  testPageContains("foo", "Hello from a FooResource!")
  testPageContains("foo/abc", "The item id is", "abc")
  testPageContains("foo/def", "The item id is", "def")

  testPageContains("scaml/defaultAttribute.scaml", "James")

  testPageContains("scaml/errors/badAttributeType.scaml",
    "error: not found: type StringT",
    "in /scaml/errors/badAttributeType.scaml near line 19 col 13")

  testPageContains("scaml/errors/badExpression.scaml",
    "error: not found: value nameX",
    "in /scaml/errors/badExpression.scaml near line 24 col 11")

  testPageContains("scaml/errors/missingAttribute.scaml", "The value for 'name' was not set")
  testPageContains("scaml/errors/missingInclude.scaml", "Could not load resource")

  testPageContains("scaml/errors/scalaCompileError.scaml",
    "error: not found: value unknown",
    "in /scaml/errors/scalaCompileError.scaml near line 25 col 17",
    "- for (i &lt;-")

  testPageContains("scaml/errors/templateCompileError.scaml",
    "Inconsistent indent level detected: intended too shallow",
    "in /scaml/errors/templateCompileError.scaml near line 23 col 4",
    "%h1 Template Compiler Error")


  testPageContains("scaml/locale.scaml", "22.0 / 7 = 3.143")
  testPageContains("scaml/optionTest.scaml", "no foo")
  testPageContains("scaml/renderObject.scaml", "Strachan")
  testPageContains("scaml/renderCaseClass.scaml", "Strachan", "<hr/>", "Chirino")
  testPageContains("scaml/renderCollection.scaml", "Strachan", "<hr/>", "Chirino")
  testPageContains("scaml/sampleTag.scaml",  "Wrapped body", "hey Hiram this is some body text!", "End of wrapped body")
  testPageContains("scaml/sampleTag2.scaml", "Wrapped body", "hey Hiram this is some body text!", "End of wrapped body")
  testPageContains("scaml/sampleTag3.scaml", "Wrapped body", "hey James this is some body text!", "End of wrapped body")
  testPageContains("scaml/simple.scaml", "Scaml is a Scala version of", "Haml")
  testPageContains("scaml/snippet.scaml", "mmm I like beer")
  testPageContains("scaml/standalone.scaml", "1 + 2 = 3")
  testPageContains("filtered.html", "This AWESOME page is rendered by the filter.")

}