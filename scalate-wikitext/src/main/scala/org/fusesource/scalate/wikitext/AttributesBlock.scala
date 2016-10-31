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
package org.fusesource.scalate
package wikitext

import util.Log

object AttributesTag extends Log; import AttributesTag._

/**
 * Allows Scalate attributes to be defined inside a confluence template.
 *
 * For example  {attributes:layout=foo.scaml } to change the layout
 */
class AttributesTag extends AbstractConfluenceTagSupport("attributes") {

  def setOption(key: String, value: String) = {
    debug("{attributes} setting %s to %s", key, value)
    val context = RenderContext()
    context.attributes(key) = value
  }

  def doTag() = {
  }
}

