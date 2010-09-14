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

object Links {

  /**
   * Converts an absolute link rom the root directory to a relative link from the current
   * request URI
   */
  def convertAbsoluteLinks(link: String, requestUri: String): String = if (link.startsWith("/")) {
    var n = link.stripPrefix("/").split('/').toList
    var r = requestUri.stripPrefix("/").split('/').toList

    // lets strip the common prefixes off
    while (n.size > 1 && r.size > 1 && n.head == r.head) {
      n = n.tail
      r = r.tail
    }

    val prefix = "../" * (r.size - 1)
    n.mkString(prefix, "/", "")
  } else {
    link
  }
}