/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.test

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.{ ConfigMap, BeforeAndAfterAllConfigMap }
import org.openqa.selenium.{ WebDriver, WebElement }
import org.openqa.selenium.internal.FindsByXPath
import org.scalatest.funsuite.AnyFunSuite

/**
 * A simple trait for testing web pages using Selenium WebDriver
 *
 * @version $Revision : 1.1 $
 */
trait WebDriverMixin extends BeforeAndAfterAllConfigMap { this: AnyFunSuite =>

  def rootUrl: String

  var webDriver: WebDriver = new HtmlUnitDriver

  def xpathDriver = webDriver.asInstanceOf[FindsByXPath]

  override protected def afterAll(configMap: ConfigMap) = webDriver.close

  def pageContains(textLines: String*): Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    var index = 0
    for (text <- textLines if index >= 0) {
      index = source.indexOf(text, index)
      if (index >= 0) {
        index += text.length
      } else {
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
    val source = pageSource
    assume(source != null, "page source was null for " + webDriver.getCurrentUrl)
    assume(source.matches(regex), "Page does not match '" + regex + "' for " + webDriver.getCurrentUrl + " when page was\n" + source)
  }

  def pageSource = webDriver.getPageSource

  def testPageContains(uri: String, textLines: String*): Unit = {
    testPage(uri) {
      pageContains(textLines: _*)
    }
  }

  def testPageNotContains(uri: String, textLines: String*): Unit = {
    testPage(uri) {
      pageNotContains(textLines: _*)
    }
  }

  def testPageMatches(uri: String, matches: String): Unit = {
    testPage(uri) {
      pageMatches(matches)
    }
  }

  def testPage(uri: String)(func: => Unit): Unit = {
    test("page: " + uri) {
      val fullUri = if (uri.startsWith("http")) { uri } else { rootUrl + uri }

      println("Loading page: " + fullUri)
      webDriver.get(fullUri)

      println("About to run test for: " + fullUri)
      func
      println("Completed test for: " + fullUri)
    }
  }

  /**
   * Returns the XPath selector which can then be used for further navigation
   */
  def xpath(selector: String): WebElement = {
    try {
      val answer = xpathDriver.findElementByXPath(selector)
      assume(answer != null, "xpath " + selector + " returned null!")
      answer
    } catch {
      case e: Exception =>
        println("Failed to find xpath: " + selector + " on page due to: " + e)
        println(pageSource)
        throw e
    }
  }

}
