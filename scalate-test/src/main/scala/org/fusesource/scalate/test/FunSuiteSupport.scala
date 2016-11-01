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

import _root_.org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{ ConfigMap, BeforeAndAfterAllConfigMap, FunSuite }
import java.io.File
import org.fusesource.scalate.util.Log

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
abstract class FunSuiteSupport
    extends FunSuite
    with BeforeAndAfterAllConfigMap
    with Log {

  /**
   * Returns the base directory of the current project
   */
  def baseDir = new File(Config.baseDir)

  override protected def beforeAll(map: ConfigMap): Unit = {
    map.get("basedir") match {
      case Some(basedir) => Config.baseDir = basedir.toString
      case _ =>
    }
    debug("using basedir: %s", Config.baseDir)
  }

}
