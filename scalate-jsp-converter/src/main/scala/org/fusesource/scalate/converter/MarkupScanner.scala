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
package org.fusesource.scalate.converter

import util.parsing.combinator.RegexParsers
import util.parsing.input.{CharSequenceReader, NoPosition, Position}
import org.fusesource.scalate.TemplateException
import org.fusesource.scalate.support.ScalaParseSupport

class MarkupScanner extends ScalaParseSupport {
  override def skipWhitespace = false

  //   ident     {nmstart}{nmchar}*
  def IDENT = (nmstart ~ rep(nmchar)) ^^ { case n ~ l => n + l.mkString("")}

  // name      {nmchar}+
  private def name = rep1(nmchar)

  // nmstart   [_a-z]|{nonascii}|{escape}
  private def nmstart = """[_a-zA-Z]""".r | nonascii | escape

  // nonascii  [^\0-\177]
  private def nonascii = """[^\x00-\xB1]""".r

  // unicode   \\[0-9a-f]{1,6}(\r\n|[ \n\r\t\f])?
  private def unicode = """\\[0-9a-fA-F]{1,6}(\r\n|[ \n\r\t\f])?""".r

  // escape    {unicode}|\\[^\n\r\f0-9a-f]
  private def escape = unicode | """\\[^\n\r\f0-9a-fA-F]""".r

  // nmchar    [_a-z0-9-]|{nonascii}|{escape}
  private def nmchar = """[_a-zA-Z0-9-]""".r | nonascii | escape

  // num       [0-9]+|[0-9]*\.[0-9]+
  private val num = """[0-9]+|[0-9]*"."[0-9]+"""

  // string    {string1}|{string2}
  def STRING = string1 | string2

  // string1   \"([^\n\r\f\\"]|\\{nl}|{nonascii}|{escape})*\"
  private val string1 = ("\"" ~> rep("""[^\n\r\f\\"]""".r | ("\\" + nl).r | nonascii | escape) <~ "\"") ^^ { case l => l.mkString("") }

  // string2   \'([^\n\r\f\\']|\\{nl}|{nonascii}|{escape})*\'
  private val string2 = ("'" ~> rep("""[^\n\r\f\']""".r | ("\\" + nl).r | nonascii | escape) <~ "'") ^^ { case l => l.mkString("") }

  // nl        \n|\r\n|\r|\f
  private val nl = """\n|\r\n|\r|\f"""

  val S = """\s+""".r
  val repS = """[\s]*""".r
  val rep1S = """[\s]+""".r

}