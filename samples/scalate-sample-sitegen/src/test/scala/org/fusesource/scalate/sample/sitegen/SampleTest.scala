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

package org.fusesource.scalate.sample.sitegen

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite

import org.fusesource.scalate.test._
import org.fusesource.scalate.wikitext.Pygmentize
import org.fusesource.scalate.util.Logging

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class SampleTest extends FunSuite with WebServerMixin with WebDriverMixin with Logging {
  override protected def beforeAll(configMap: Map[String, Any]) = {
    System.setProperty("scalate.mode", "development")
    super.beforeAll(configMap)
  }

  testPageContains("index.conf", "Sample WebSite")
  testPageContains("code.conf", "Java code sample")

  if (Pygmentize.isInstalled) {
    testPageContains("pygmentizeExample.conf", "Pygmentize sample")
  } else {
    warn("Pygmentize not installed so ignoring the tests")
  }
}
