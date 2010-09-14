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
package filter

import com.petebevin.markdown.MarkdownProcessor

/**
 * Renders markdown syntax.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object MarkdownFilter extends Filter with TemplateEngineAddOn {
  val markdownProcessor = new MarkdownProcessor

  def filter(context: RenderContext, content: String) = {
    markdownProcessor.markdown(content).stripLineEnd
  }

  /**
   * Add the markdown filter tot he template engine.
   */
  def apply(te: TemplateEngine) = {
    te.filters += "markdown" -> MarkdownFilter
    te.pipelines += "md" -> List(MarkdownFilter)
    te.pipelines += "markdown" -> List(MarkdownFilter)
  }

}