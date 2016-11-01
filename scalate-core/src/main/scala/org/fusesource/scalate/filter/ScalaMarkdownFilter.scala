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
package filter

import org.fusesource.scalate.{ TemplateEngine, TemplateEngineAddOn }
import org.fusesource.scalamd.Markdown

/**
 * Renders markdown syntax.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ScalaMarkdownFilter extends Filter with TemplateEngineAddOn {

  def filter(context: RenderContext, content: String) = {
    Markdown.apply(content).stripLineEnd
  }

  /**
   * Add the markdown filter to the template engine.
   */
  def apply(te: TemplateEngine) = {
    te.filters += "markdown" -> ScalaMarkdownFilter
    te.pipelines += "md" -> List(ScalaMarkdownFilter)
    te.pipelines += "markdown" -> List(ScalaMarkdownFilter)
  }
}