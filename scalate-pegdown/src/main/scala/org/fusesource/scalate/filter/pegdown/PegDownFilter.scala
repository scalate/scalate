/*
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
package org.fusesource.scalate.filter.pegdown

import org.fusesource.scalate.{ TemplateEngineAddOn, RenderContext, TemplateEngine }
import org.fusesource.scalate.filter.Filter
import org.fusesource.scalate.util.ObjectPool
import org.pegdown.{ Extensions, PegDownProcessor }

object PegDownFilter extends TemplateEngineAddOn {

  /**
   * Add the markdown filter to the template engine.
   */
  def apply(te: TemplateEngine) = {
    val filter = new PegDownFilter()
    filter.registerWith(te)
  }
}

/**
 * Renders markdown syntax with multi-markdown like extras.
 *
 * @author <a href="mailto:stuart.roebuck@gmail.com">Stuart Roebuck</a>
 */
class PegDownFilter(val extensions: Int = Extensions.ABBREVIATIONS |
  Extensions.AUTOLINKS |
  Extensions.DEFINITIONS |
  Extensions.FENCED_CODE_BLOCKS |
  Extensions.QUOTES |
  Extensions.SMARTS |
  Extensions.TABLES |
  Extensions.WIKILINKS, processorPoolSize: Int = 10) extends Filter {

  private[this] val pegDownProcessorPool = new ObjectPool[PegDownProcessor](processorPoolSize, () => {
    new PegDownProcessor(extensions)
  })

  def filter(context: RenderContext, content: String) = {
    val pegDownProcessor = pegDownProcessorPool.fetch()
    try {
      pegDownProcessor.markdownToHtml(content).stripLineEnd
    } finally {
      pegDownProcessorPool.release(pegDownProcessor)
    }
  }

  def registerWith(te: TemplateEngine): Unit = {
    te.filters += "multimarkdown" -> this
    te.pipelines += "mmd" -> List(this)
    te.pipelines += "multimarkdown" -> List(this)
  }
}
