package org.fusesource.scalate.sample

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import java.lang.String
import collection.immutable.Map
import org.scalatest.{FunSuite, Suite, BeforeAndAfterAll}

/**
 * A simple trait for testing web pages using Selenium WebDriver
 *
 * @version $Revision : 1.1 $
 */
trait WebDriverMixin extends BeforeAndAfterAll {
  this: FunSuite =>

  def rootUrl: String

  val webDriver = new HtmlUnitDriver

  override protected def afterAll(configMap: Map[String, Any]) = webDriver.close

  def pageContains(text: String): Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    assume(source.contains(text), "Page does not contain '" + text + "' for " + webDriver.getCurrentUrl + " when page was\n" + source)
  }


  def testPage(uri: String)(func: => Unit) {
    test("page: " + uri) {
      val fullUri = if (uri.startsWith("http")) {uri} else {rootUrl + uri}

      println("Loading page: " + fullUri)
      webDriver.get(fullUri)

      println("About to run test for: " + fullUri)
      func
      println("Completed test for: " + fullUri)
    }
  }

}