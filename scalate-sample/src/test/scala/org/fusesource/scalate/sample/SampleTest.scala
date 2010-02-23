package org.fusesource.scalate.sample

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite}

import org.fusesource.scalate.test._

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

  testPageContains("ssp/customLayout.ssp", "layout header goes here...", "Custom page", "This is some text", "layout footer goes here...")
  testPageContains("ssp/defaultAttribute.ssp", "James")
  testPageContains("ssp/exampleIncludes.ssp", "included from /includes/something.jsp",
    "included from /ssp/child/foo.ssp", "included from /includes/something.jsp", "Finished including!")

  testPageContains("ssp/locale.ssp", "22.0 / 7 = 3.143")
  testPageContains("ssp/missingAttribute.ssp", "The value for 'name' was not set")

  testPage("ssp/noLayout.ssp") {
    pageContains("No Layout", "This page has no separate layout")
    pageNotContains("stylesheet", "text/css", "style.css")
  }

//  testPageContains("ssp/renderObject.ssp", "Strachan")
  testPageContains("ssp/renderCaseClass.ssp", "Strachan", "<hr/>", "Chirino")
  testPageContains("ssp/renderCollection.ssp", "Strachan", "<hr/>", "Chirino")
  testPageContains("ssp/sampleTag.ssp", "Wrapped body", "this is some body!", "End of wrapped body")
  testPageContains("ssp/sampleTag2.ssp", "Wrapped body", "this is the body!", "End of wrapped body")
  testPageContains("ssp/simple.ssp", "1 + 2 = 3")
  testPageContains("ssp/snippet.ssp", "mmm I like beer")

  testPageContains("foo", "Hello from a FooResource!")
  testPageContains("foo/abc", "The item id is", "abc")
  testPageContains("foo/def", "The item id is", "def")

  testPageContains("scaml/simple.scaml", "Scaml is a Scala version of", "Haml")
  testPageContains("scaml/defaultAttribute.scaml", "James")
  testPageContains("scaml/locale.scaml", "22.0 / 7 = 3.143")
  testPageContains("scaml/missingAttribute.scaml", "The value for 'name' was not set")
  testPageContains("scaml/standalone.scaml", "1 + 2 = 3")
  testPageContains("scaml/snippet.scaml", "mmm I like beer")
  testPageContains("scaml/renderObject.scaml", "Strachan")
  testPageContains("scaml/renderCaseClass.scaml", "Strachan", "<hr/>", "Chirino")
  testPageContains("scaml/renderCollection.scaml", "Strachan", "<hr/>", "Chirino")
  testPageContains("scaml/sampleTag.scaml", "Wrapped body", "this is some body!", "End of wrapped body")
  testPageContains("scaml/sampleTag2.scaml", "Wrapped body", "this is the body!", "End of wrapped body")

}