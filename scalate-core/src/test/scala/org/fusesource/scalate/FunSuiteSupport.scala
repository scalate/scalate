package org.fusesource.scalate

import _root_.org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import scuery.Transformer
import util.Logging
import java.io.File
import java.lang.String
import collection.immutable.Map
import xml.NodeSeq

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
abstract class FunSuiteSupport extends FunSuite with Logging with BeforeAndAfterAll {
  protected var _basedir = "."

  /**
   * Returns the base directory of the current project
   */
  def baseDir = {
    new File(_basedir)
  }


  override protected def beforeAll(map: Map[String, Any]): Unit = {
    _basedir = map.get("basedir") match {
      case Some(basedir) => basedir.toString
      case _ => System.getProperty("basedir", ".")
    }
    debug("using basedir: " + _basedir)
  }


  def assertSize(selector: String, result: NodeSeq, expected: Int): Unit = {
    // for $ on nodes
    import Transformer._

    expect(expected, "number of elements matching: " + selector) {result.$(selector).size}
  }

  /**
   * Asserts that the text value of the given selector matches the expected string
   */
  def assertText(selector: String, result: NodeSeq, expected: String): Unit = {
    // for $ on nodes
    import Transformer._

    expect(expected, "text of elements matching: " + selector) {result.$(selector).text}
  }
}