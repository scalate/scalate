package org.fusesource.scalate.test

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

/*
  def pageContains(text: String): Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    assume(source.contains(text), "Page does not contain '" + text + "' for " + webDriver.getCurrentUrl + " when page was\n" + source)
  }
*/

  def pageContains(textLines: String*): Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    var index = 0
    for (text <- textLines if index >= 0) {
      index = source.indexOf(text, index)
      if (index >= 0) {
        index += text.length
      }
      else {
        assume(false, "Page does not contain '" + text + "' for " + webDriver.getCurrentUrl + " when page was\n" + source)
      }
    }
  }

  def pageNotContains(textLines: String*): Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    for (text <- textLines) {
      val index = source.indexOf(text)
      if (index >= 0) {
        assume(false, "Page contains '" + text + "' at index " + index + " for " + webDriver.getCurrentUrl + " when page was\n" + source)
      }
    }
  }

  def pageMatches(regex: String): Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    assume(source.matches(regex), "Page does not match '" + regex + "' for " + webDriver.getCurrentUrl + " when page was\n" + source)
  }


  def testPageContains(uri: String, textLines:String*) {
    testPage(uri) {
      pageContains(textLines:_*)
    }
  }
  
  def testPageNotContains(uri: String, textLines:String*) {
    testPage(uri) {
      pageNotContains(textLines:_*)
    }
  }

  def testPageMatches(uri: String, matches:String) {
    testPage(uri) {
      pageMatches(matches)
    }
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