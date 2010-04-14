package org.fusesource.scalate.test

import _root_.org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import java.io.File
import java.lang.String
import collection.immutable.Map
import org.fusesource.scalate.util.Logging

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
abstract class FunSuiteSupport extends FunSuite with Logging with BeforeAndAfterAll {

  /**
   * Returns the base directory of the current project
   */
  def baseDir =  new File(Config.baseDir)

  override protected def beforeAll(map: Map[String, Any]): Unit = {
    map.get("basedir") match {
      case Some(basedir) => Config.baseDir = basedir.toString
      case _ =>
    }
    debug("using basedir: " + Config.baseDir)
  }
}