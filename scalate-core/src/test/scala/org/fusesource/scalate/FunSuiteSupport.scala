package org.fusesource.scalate


import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSuite
import _root_.org.scalatest.junit.JUnitRunner
import util.Logging
import java.io.File

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
abstract class FunSuiteSupport extends FunSuite with Logging {
  import Asserts._

  /**
   * Returns the base directory of the current project
   */
  def baseDir = {
    // TODO this is a bit of a back. if basedir not set assume basedir is the scalate-core module
    new File(System.getProperty("basedir", "scalate-core"))
  }
}