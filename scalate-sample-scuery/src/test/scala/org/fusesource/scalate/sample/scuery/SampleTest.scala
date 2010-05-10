package org.fusesource.scalate.sample.scuery

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
    pageContains("Bookstore")
  }

  testPageContains("id/item1", "Title1", "Author1", "item1")
  testPageContains("id/item2", "Title2", "Author2", "item2")

}