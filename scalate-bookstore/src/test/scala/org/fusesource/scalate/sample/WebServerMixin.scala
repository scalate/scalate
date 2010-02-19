package org.fusesource.scalate.sample

import org.scalatest.{Suite, BeforeAndAfterAll}

/**
 * A trait which boots up a JettyServer and uses it for all the test cases in this class
 * 
 * @version $Revision: 1.1 $
 */
trait WebServerMixin extends BeforeAndAfterAll {
  this: Suite =>

  val webServer = new JettyServer

  override protected def beforeAll(configMap: Map[String, Any]) = webServer.start

  override protected def afterAll(configMap: Map[String, Any]) = webServer.stop

  def rootUrl = webServer.rootUrl
}