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

package scalate

import org.fusesource.scalate.util.Logging
import org.fusesource.scalate.{TemplateEngine, MockBootstrap}

/**
 * A simple boot strap mechanism to act as a simple place to host initialisation code which can then be shared across
 * web apps or static site gen
 */
class Boot(engine: TemplateEngine) extends Logging {

  def run: Unit = {
    info("scalate bootstrap starting with template engine: " + engine)
    MockBootstrap.initialised = true
  }


  override def toString = "Boot(" + engine + ")"
}