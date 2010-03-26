package org.fusesource.scalate


import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSuite
import _root_.org.scalatest.junit.JUnitRunner
import util.Logging

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
abstract class FunSuiteSupport extends FunSuite with Logging {
  import Asserts._
}