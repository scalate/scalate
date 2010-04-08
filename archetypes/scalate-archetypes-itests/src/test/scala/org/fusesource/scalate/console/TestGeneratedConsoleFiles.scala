package org.fusesource.scalate.console

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.{FunSuite}

import _root_.org.fusesource.scalate.test._

/**
 * Tests the generated console files which are created by the ConsoleTest
 *
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class TestGeneratedConsoleFiles extends FunSuite with WebServerMixin with WebDriverMixin {

  // Simple Resource created
  testPageContains("sample", "My name is", "Some Name")
}