package org.fusesource.scalate.sample

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import java.lang.String
import collection.immutable.Map
import org.openqa.selenium.htmlunit.HtmlUnitDriver

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class SampleTest extends FunSuite with WebServerMixin with WebDriverMixin {

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

  testPage("ssp/renderCaseClass.ssp") {
    println("Testing if models get rendered")
    pageContains("Hiram")
    pageContains("James")
  }

  testPage("ssp/missingAttribute.ssp") {
    println("Testing missing attributes!")
    pageContains("The value for 'name' was not set")
  }

  testPage("ssp/exampleIncludes.ssp") {
    println("Testing if include contains stuff!!!")
    pageContains("included from /includes/something.jsp")
    pageContains("included from /ssp/child/foo.ssp")
  }

  def testPage(uri: String)(func: => Unit) {
    test("page: " + uri) {
      val fullUri = if (uri.startsWith("http")) { uri} else { rootUrl + uri}
      
      println("Loading page: " + fullUri)
      webDriver.get(fullUri)

      println("About to run test for: " + fullUri)
      func
      println("Completed test for: " + fullUri)
    }
  }
}