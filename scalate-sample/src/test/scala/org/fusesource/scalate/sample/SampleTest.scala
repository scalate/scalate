package org.fusesource.scalate.sample

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite}

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class SampleTest extends FunSuite with WebServerMixin with WebDriverMixin {

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

  testPageContains("sampleServlet", "The foo is: Foo(")

  testPageContains("ssp/defaultAttribute.ssp", "James")
  testPageContains("ssp/exampleIncludes.ssp", "included from /includes/something.jsp",
    "included from /ssp/child/foo.ssp", "included from /includes/something.jsp", "Finished including!")

  testPageContains("ssp/locale.ssp", "22.0 / 7 = 3.143")
  testPageContains("ssp/missingAttribute.ssp", "The value for 'name' was not set")
  testPageContains("ssp/standalone.ssp", "1 + 2 = 3")
  testPageContains("ssp/snippet.ssp", "mmm I like beer")
  testPageContains("ssp/renderObject.ssp", "Strachan")
  testPageContains("ssp/renderCaseClass.ssp", "Strachan", "<hr/>", "Chirino")
  testPageContains("ssp/renderCollection.ssp", "Strachan", "<hr/>", "Chirino")
  testPageContains("ssp/sampleTag.ssp", "Wrapped body", "this is some body!", "End of wrapped body")
  testPageContains("ssp/sampleTag2.ssp", "Wrapped body", "this is the body!", "End of wrapped body")

  testPageContains("foo", "Hello from a FooResource!")
  testPageContains("foo/abc", "The item id is", "abc")
  testPageContains("foo/def", "The item id is", "def")

  testPageContains("shaml/simple.shaml", "SHAML is a Scala version of", "HAML")
}