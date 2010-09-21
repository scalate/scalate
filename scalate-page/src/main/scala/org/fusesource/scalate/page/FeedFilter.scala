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
package org.fusesource.scalate.page

import org.fusesource.scalate._
import org.fusesource.scalate.filter.Filter
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Generates an RSS feed
 */
object FeedFilter extends Filter with TemplateEngineAddOn {

  // Fri, 30 Jul 2010 17:42:39 +0000
  val dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")

  def filter(context: RenderContext, content: String) = {
    def attr(name: String, defaultValue: => String = "") = context.attributeOrElse(name, defaultValue)


    // lets disable layouts
    context.attributes("layout") = ""

    val page = PageFilter.parse(context, content)
    context.withAttributes(page.headers) {


      val helper = new PageHelper(context)

      val pages = helper.blogPosts

      val pubDate = format(new Date())

      val xml = <rss version="2.0">
        <channel>
          <title>
            {attr("title")}
          </title>
          <link>
            {attr("link")}
          </link>
          <description>
            {attr("description")}
          </description>
          <pubDate>
            {pubDate}
          </pubDate>
          <lastBuildDate>
            {pubDate}
          </lastBuildDate>
          <generator>Scalate - http://scalate.fusesource.org/</generator>{pages.map {
          page =>
            <item>
              <title>
                {page.title}
              </title>
              <link>
                {page.link}
              </link>
              <description></description>
            </item>
        }}
        </channel>
      </rss>

      "<?xml version='1.0' encoding='utf-8' ?>\n" + xml
    }
  }

  def apply(te: TemplateEngine) = {
    te.filters += "feed" -> FeedFilter
    te.pipelines += "feed" -> List(FeedFilter)
  }

  def format(date: Date) = dateFormat.format(date)

}


