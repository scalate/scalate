package org.fusesource.scalate.test

import org.scalatest.{Suite, BeforeAndAfterAll}


/**
 * A trait which boots up a JettyServer and uses it for all the test cases in this class
 * 
 * @version $Revision: 1.1 $
 */
trait WebServerMixin extends BeforeAndAfterAll {
  this: Suite =>

  val webServer = new JettyServer

  override protected def beforeAll(configMap: Map[String, Any]): Unit = {
    configMap.get("basedir") match {
      case Some(basedir) => val text = basedir.toString
      println("Setting basedir to: " + text)
      Config.baseDir = text
      println("Basedir is now: " + Config.baseDir)

      case _ =>
    }

    webServer.start
  }

  override protected def afterAll(configMap: Map[String, Any]) = webServer.stop

  def rootUrl = webServer.rootUrl
}