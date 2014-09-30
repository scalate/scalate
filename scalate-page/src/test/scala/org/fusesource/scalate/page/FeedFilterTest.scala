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
package org.fusesource.scalate.page

import org.fusesource.scalate.test.TemplateTestSupport
import xml.{NodeSeq, XML}

class FeedFilterTest extends TemplateTestSupport {

  test("feed filter") {
    val output = engine.layout("/blog/index.feed")
    if (showOutput) {
      info("Output: " + output)
    }

    val xml = XML.loadString(output)

    def trimText(nodes: NodeSeq) = nodes.text.trim

    val channel = xml \\ "rss" \\ "channel"
    expect("The Scalate Blog") { trimText(channel \ "title") }
    //expect("Scalate Team") { trimText(channel \ "author") }
    expect("http://scalate.github.io/scalate/blog/") { trimText(channel \ "link") }
    expect("Some text goes here") { trimText(channel \ "description") }
  }
}