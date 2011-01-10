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

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage
import java.{util => ju}
import org.fusesource.scalate.{TemplateEngineAddOn, TemplateEngine}
import org.fusesource.scalate.filter.{Pipeline, Filter}

object WikiTextFilter {

  /**
   * Returns the default wiki file extensions
   */
  var wikiFileExtensions: Set[String] = Set("conf", "md", "markdown", "textile", "page")
}

abstract class WikiTextFilter extends Filter {
  def filter(context: RenderContext, content: String): String = {
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
   * Should we fix up wiki page links which can be wrong if we export wiki pages from a wiki
   * such as Confluence and then move the files around on disk into directories
   */
  var fixWikiLinks = true

  /**
   * Add the markdown filter to the template engine.
   */
  def apply(te: TemplateEngine) = {
    val confluenceFilter = if (fixWikiLinks) {
      val extensions = te.extensions ++ WikiTextFilter.wikiFileExtensions
      Pipeline(List(ConfluenceFilter, new SwizzleLinkFilter(te.sourceDirectories, extensions)))
    } else {
      ConfluenceFilter
    }

    te.filters += "conf" -> confluenceFilter
    te.pipelines += "conf" -> List(confluenceFilter)
  }
}

/**
 * Renders a Textile filter
 */
object TextileFilter extends WikiTextFilter with TemplateEngineAddOn {
  def markupLanguage = new org.eclipse.mylyn.wikitext.textile.core.TextileLanguage

  /**
   * Add the textile filter to the template engine.
   */
  def apply(te: TemplateEngine) = {
    te.filters += "textile" -> TextileFilter
    te.pipelines += "textile" -> List(TextileFilter)
  }
}
