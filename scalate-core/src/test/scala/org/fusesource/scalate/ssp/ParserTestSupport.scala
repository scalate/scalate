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

package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate.FunSuiteSupport
import collection.mutable.HashMap
import org.fusesource.scalate.support.Text

/**
 * @version $Revision : 1.1 $
 */
abstract class ParserTestSupport extends FunSuiteSupport {

  implicit def stringToText(x: String) = Text(x)

  def countTypes(lines: List[PageFragment]): HashMap[Class[_], Int] = {
    val map = new HashMap[Class[_], Int]
    for (line <- lines) {
      val key = line.getClass
      map(key) = map.getOrElse(key, 0) + 1
    }
    map
  }


  def assertAttribute(lines: List[PageFragment], expectedParam: AttributeFragment) = {
    val attribute = lines.find {
      case d: AttributeFragment => true
      case _ => false
    }
    expect(Some(expectedParam)) {attribute}

    lines
  }

  def assertValid(text: String): List[PageFragment] = {
    debug("Parsing...")
    debug(text)
    debug("")

    val lines = (new SspParser).getPageFragments(text)
    for (line <- lines) {
      debug("=> " + line)
    }
    debug("")
    lines
  }

}
