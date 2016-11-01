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
package wikitext

import org.eclipse.mylyn.internal.wikitext.confluence.core.block.ParameterizedBlock
import java.lang.String
import java.util.regex.Matcher

/**
 * Base class for any confluence tag which is a single tag not a AbstractConfluenceDelimitedBlock
 */
abstract class AbstractConfluenceTagSupport(tag: String) extends ParameterizedBlock {
  val startPattern = ("\\s*\\{" + tag + "(?::([^\\}]+))?\\}\\s*(.+)?").r.pattern
  var matcher: Matcher = _
  var blockLineNumber = 0

  def processLineContent(line: String, offset: Int) = {
    blockLineNumber += 1
    if (blockLineNumber > 1) {
      setClosed(true)
      0
    } else {
      val options = matcher.group(1)
      setOptions(options)
      doTag()
      setClosed(true)
      matcher.start(2)
    }
  }

  def canStart(line: String, lineOffset: Int) = {
    blockLineNumber = 0
    matcher = startPattern.matcher(line)
    if (lineOffset > 0) {
      matcher.region(lineOffset, line.length)
    }
    if (matcher.matches()) {
      true
    } else {
      matcher = null
      false
    }
  }

  def doTag(): Unit
}

