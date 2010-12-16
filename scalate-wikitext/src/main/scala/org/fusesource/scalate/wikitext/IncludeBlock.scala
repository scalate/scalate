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

package org.fusesource.scalate
package wikitext

import util.{Files, Logging}

/**
 * Implements the "include" macro
 */
class IncludeTag extends AbstractConfluenceTagSupport("include") with Logging {
  var uri: String = _

  override def setOption(option: String) = {
    uri = option
  }

  def setOption(key: String, value: String) =
    Blocks.unknownAttribute(key, value)


  def doTag() = {
    val realUri = SwizzleLinkFilter.findWikiFileUri(uri).getOrElse(uri)

    debug("{include} is now going to include URI '" + uri + "' found to map to '" + realUri + "'")

    val context = RenderContext()
    val ex = Files.extension(realUri)
    val engine = context.engine

    context.withUri(realUri) {
      val output = if (engine.extensions.contains(ex)) {
        val template = engine.load(realUri)
        context.capture(template)
      } else {
        context.
        engine.resourceLoader.resource(realUri) match {
          case Some(r) =>
            warn("Using non-template or wiki markup  '" + realUri + "' from {include:" + uri + "}")
            r.text
          case _ =>
            warn("Could not find include '" + realUri + "' from {include:" + uri + "}")
            ""
        }

      }
      builder.charactersUnescaped(output)
    }
  }
}
