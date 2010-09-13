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

import org.fusesource.scalate.filter.Filter
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser
import org.eclipse.mylyn.wikitext.core.parser.markup.{Block, MarkupLanguage}
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage
import java.{util => ju}
import org.fusesource.scalate.{TemplateEngineAddOn, TemplateEngine}

abstract class WikiTextFilter extends Filter {
  def filter(content: String): String = {
    val parser = new MarkupParser
    parser.setMarkupLanguage(markupLanguage)
    parser.parseToHtml(content).split("<body>") match {
      case Array(head, rest) => rest.stripSuffix("</body></html>")
      case Array(text) => text
    }
  }

  def markupLanguage: MarkupLanguage
}

/**
 * Renders a Confluence filter
 */
object ConfluenceFilter extends WikiTextFilter with TemplateEngineAddOn {
  def markupLanguage = new ScalateConfluenceLanguage


  /**
   * Add the markdown filter tot he template engine.
   */
  def apply(te: TemplateEngine) = {
    te.filters += "conf"->ConfluenceFilter
    te.pipelines += "conf"->List(ConfluenceFilter)
  }
}
