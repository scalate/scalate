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

package org.fusesource.scalate.wikitext

import java.{util => ju}
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.AbstractConfluenceDelimitedBlock
import org.fusesource.scalate.util.Logging
import java.lang.String
import org.fusesource.scalate.RenderContext

/**
 * Implements the "include" macro
 */
class IncludeBlock extends AbstractConfluenceDelimitedBlock("include") with Logging {
  var uri: String = _

  override def setOption(option: String) = {
    uri = option
  }

  def setOption(key: String, value: String) =
    Blocks.unknownAttribute(key, value)

  def beginBlock = {
  }

  def handleBlockContent(text: String) = {
  }

  def endBlock = {
    val context = RenderContext()
    val realUri = SwizzleLinkFilter(context).findConfluenceFile(uri, context.requestUri).getOrElse(uri)
    debug("{include} is now going to include URI '" + uri + "' found to map to '" + realUri + "'")
    context.include(realUri)
  }
}
