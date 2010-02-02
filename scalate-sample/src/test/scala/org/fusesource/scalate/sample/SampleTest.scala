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

  testPage("ssp/missingAttribute.ssp") {
    pageContains("No attribute called 'name' was available")
  }

  testPage("ssp/exampleIncludes.ssp") {
    pageContains("included from /includes/something.jsp")
    pageContains("included from /ssp/child/foo.ssp")
  }
}