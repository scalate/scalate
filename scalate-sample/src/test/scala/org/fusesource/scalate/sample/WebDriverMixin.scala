package org.fusesource.scalate.sample

import org.openqa.selenium.htmlunit.HtmlUnitDriver
import java.lang.String
import collection.immutable.Map
import org.scalatest.{Suite, AbstractSuite, BeforeAndAfterAll}

/**
 * @version $Revision: 1.1 $
 */

trait WebDriverMixin extends BeforeAndAfterAll{
  this: Suite =>

  val webDriver = new HtmlUnitDriver

  override protected def afterAll(configMap: Map[String, Any]) = webDriver.close

  def pageContains(text: String) : Unit = {
    val source = webDriver.getPageSource
    assume(source != null, "page source was null for "+ webDriver.getCurrentUrl)
    assume(source.contains(text), "Page does not contain '" + text + "' for " + webDriver.getCurrentUrl + " when page was\n" + source)
  }
}