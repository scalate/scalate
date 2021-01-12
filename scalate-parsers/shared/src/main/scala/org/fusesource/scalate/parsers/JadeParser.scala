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
package org.fusesource.scalate.parsers

import scala.util.parsing.input.CharSequenceReader

/**
 * <p>
 * Parser for a more concise version of haml/scaml inspired by jade:
 * http://github.com/visionmedia/jade
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class JadeParser extends ScamlParser {

  override def full_element_statement: Parser[Element] =
    opt(tag_ident) ~ attributes ~ opt(trim) <~ ("/" ~! opt_space ~ nl) ^^ {
      case (tag ~ attributes ~ wsc) => Element(tag, attributes, None, List(), wsc, true)
    } |
      opt(tag_ident) ~ attributes ~ opt(trim) ~ element_text ~ statement_block ^^ {
        case ((tag ~ attributes ~ wsc ~ text) ~ body) => Element(tag, attributes, text, body, wsc, false)
      }

  override def element_statement: Parser[Element] = full_element_statement

  override def text_statement = (
    prefixed("""\""", literal_text(None)) |
    prefixed("&==" ~ opt_space, literal_text(Some(true))) |
    prefixed("!==" ~ opt_space, literal_text(Some(false))) |
    prefixed("&" ~ space, literal_text(Some(true))) |
    prefixed("!" ~ space, literal_text(Some(false))) |
    prefixed("|" ~ opt_space, literal_text(None)) |
    guarded("<", literal_text(None))) <~ any_space_then_nl

}

/* TODO
import org.fusesource.scalate.util.IOUtil

import java.io.File
object JadeParser {
  def main(args: Array[String]) = {
    val in = IOUtil.loadTextFile(new File(args(0)))
    val p = new JadeParser
    println(p.phrase(p.parser)(new CharSequenceReader(in)))
  }
}


 */
