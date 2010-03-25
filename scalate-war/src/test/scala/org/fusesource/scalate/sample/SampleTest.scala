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

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

/*
  testPageContains("sampleServlet", "The foo is: Foo(")
*/
}