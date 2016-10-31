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

import org.scalatest.{ ConfigMap, Suite, BeforeAndAfterAll }

/**
 * A trait which boots up a JettyServer and uses it for all the test cases in this class
 *
 * @version $Revision: 1.1 $
 */
trait WebServerMixin extends BeforeAndAfterAll {
  this: Suite =>

  val webServer = new JettyServer

  override protected def beforeAll(configMap: ConfigMap): Unit = {
    configMap.get("basedir") match {
      case Some(basedir) =>
        val text = basedir.toString
        println("Setting basedir to: " + text)
        Config.baseDir = text
        println("Basedir is now: " + Config.baseDir)

      case _ =>
    }

    webServer.start
  }

  override protected def afterAll(configMap: ConfigMap) = webServer.stop

  def rootUrl = webServer.rootUrl
}