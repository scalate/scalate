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
package org.fusesource.scalate.util

import scala.xml._
import scala.xml.parsing.ConstructingParser
import scala.io.Source

/**
 * @version $Revision : 1.1 $
 */

object XmlHelper {
  val log = Log(getClass); import log._

  /**
   * Parsers some markup which might not be a single Xml document
   * by wrapping it in a root XML element first
   */
  def textToNodeSeq(text: String): NodeSeq = {
    debug("parsing markup: " + text)

    val src = Source.fromString("<p>" + text + "</p>")

    // lets deal with HTML entities
    // lets preserve whitespace for <pre> stuff to avoid trimming indentation with code
    object parser extends ConstructingParser(src, true /* keep ws*/ ) {
      override def replacementText(entityName: String): io.Source = {
        entityName match {
          ///case "nbsp" => io.Source.fromString("\u0160");
          case "nbsp" => io.Source.fromString("<![CDATA[&nbsp;]]>");
          case _ => super.replacementText(entityName);
        }
      }
      nextch(); // !!important, to initialize the parser
    }
    parser.document().docElem.child
    /*
        val cpa = ConstructingParser.fromSource(src, false);
        cpa.document().docElem.child
    */
  }

}
