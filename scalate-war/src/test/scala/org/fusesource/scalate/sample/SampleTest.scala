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

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

/*
  testPageContains("sampleServlet", "The foo is: Foo(")
*/
}