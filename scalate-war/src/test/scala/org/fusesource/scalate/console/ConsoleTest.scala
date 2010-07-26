/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package org.fusesource.scalate.console

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.{FunSuite}

import _root_.org.fusesource.scalate.test._
import java.lang.String
import collection.immutable.Map
import java.io.File

/**
 * @version $Revision : 1.1 $
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


  override protected def beforeAll(configMap: Map[String, Any]) = {
    super.beforeAll(configMap)

    // lets force generated files into a new temporary directory
    if (System.getProperty("scalate.generate.src", "").length == 0) {
      val file = File.createTempFile("scalate-gen-src-", ".dir")
      file.delete
      file.mkdirs
      val genSrcDir = file.getPath
      info("setting source generation directory to: " + genSrcDir)
      System.setProperty("scalate.generate.src", genSrcDir)
    }
  }
}