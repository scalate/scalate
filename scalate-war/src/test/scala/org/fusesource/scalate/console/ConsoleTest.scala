package org.fusesource.scalate.console

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.{FunSuite}

import _root_.org.fusesource.scalate.test._

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ConsoleTest extends FunSuite with WebServerMixin with WebDriverMixin {

  test("home page") {
    webDriver.get(rootUrl)
    pageContains("Scalate")
  }

  test("create Simple Resource") {
    webDriver.get(rootUrl)

    // lets find the console link
    xpath("//a[@class = 'consoleLink']").click

    xpath("//a[@title = 'Create Simple Resource']").click

    xpath("//form[@class='createArchetype']").submit

    // TODO use a trim in an xpath
    pageContains("Try the New Resource")

    // Note we now need to test the generated resource after we restart the web app
    // which requires rebuilding the genreated scala files
    // so see the TestGeneratedConsoleFiles.scala for the rest of this test 
    // which we run on the generated archetypes
  }
}