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

package org.fusesource.scalate.boot

import org.fusesource.scalate.util.{Log, Logging}

/**
 * Helper class to help test that we can write custom bootstrap code
 * using the ScalatePackage mechanism
 */
object BootSample {
  val log = Log(getClass); import log._

  var initialised = false

  def boot: Unit = {
    if (!initialised) {
      info("Startup up template package bootstrap!")
      println("Startup up template package bootstrap!")
      initialised = true
    }
  }
}