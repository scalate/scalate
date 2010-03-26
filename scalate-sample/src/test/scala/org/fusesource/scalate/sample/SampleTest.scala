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

  testPageContains("ssp/renderObject.ssp", "Strachan")

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

  testPageContains("sampleServlet", "The foo is: Foo(")

  testPageContains("ssp/capture.ssp", "Hello James", "Sample 2", "Hello James")
  testPageContains("ssp/customLayout.ssp", "layout header goes here...", "Custom page", "This is some text", "layout footer goes here...")
  testPageContains("ssp/defaultAttribute.ssp", "James")
  testPageContains("ssp/exampleIncludes.ssp", "included from /includes/something.jsp",
    "included from /ssp/child/foo.ssp", "included from /includes/something.jsp", "Finished including!")

  testPageContains("ssp/ifExpression.ssp", "x = 1", "x is 1", "x = 2", "x is not 1")
  testPageContains("ssp/ifExpression2.ssp", "x = 1", "x is 1", "x = 2", "x is not 1")
  testPageContains("ssp/locale.ssp", "22.0 / 7 = 3.143")
  testPageContains("ssp/matchExpression.ssp", "i = 1", "i is 1", "i = 2", "i is 2", "i = 3", "i is something")
  testPageContains("ssp/missingAttribute.ssp", "The value for 'name' was not set")

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

  testPageContains("bad", "error: not found: value unknown")
  testPageContains("foo", "Hello from a FooResource!")
  testPageContains("foo/abc", "The item id is", "abc")
  testPageContains("foo/def", "The item id is", "def")

  testPageContains("scaml/defaultAttribute.scaml", "James")
  testPageContains("scaml/errors/templateCompileError.scaml", "Inconsistent indent level detected: intended too shallow", "%h1 Template Compiler Error")
  testPageContains("scaml/errors/scalaCompileError.scaml", "error: not found: value unknown", "- for (i &lt;-")
  testPageContains("scaml/locale.scaml", "22.0 / 7 = 3.143")
  testPageContains("scaml/missingAttribute.scaml", "The value for 'name' was not set")
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

}