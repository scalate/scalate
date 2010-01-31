/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 * Copyright (C) 2010, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.fusesource.scalate.ssp

import scala.util.parsing.combinator._
import java.io.Reader
sealed abstract class PageFragment()
case class CommentFragment(comment: String) extends PageFragment
case class DollarExpressionFragment(code: String) extends PageFragment
case class ExpressionFragment(code: String) extends PageFragment
case class ScriptletFragment(code: String) extends PageFragment
case class TextFragment(text: String) extends PageFragment

case class AttributeFragment(name: String, className: String, defaultValue: Option[String]) extends PageFragment {
  def isScala28 = true

  def methodArgumentCode = name + ": " + className + (if (isScala28) {
    if (defaultValue.isEmpty) {""} else {" = "+defaultValue.get}
  } else {""})

  def valueCode(context : String) = "val " + name + " = " + (defaultValue match {
    case Some(expression) => context + ".attributeOrElse[" + className + "](\"" + name + "\", " + expression + ")"
    case None => context + ".attribute[" + className + "](\"" + name + "\")"
  })
}


class SspParser extends JavaTokenParsers {
  def parse(in: Reader) = {
    parseAll(lines, in)
  }

  def parse(in: CharSequence) = {
    parseAll(lines, in)
  }

  def lines = rep(commentFragment | declarationFragment | expressionFragment | scriptletFragment | dollarExpressionFragment | textFragment) ^^ {
    case lines => lines
  }

  def textFragment = upToSpecialCharacter ^^ {
    case a => TextFragment(a.toString)
  }

  def dollarExpressionFragment = parser("${", "}", {DollarExpressionFragment(_)})

  def commentFragment = parser("<%--", "--%>", {CommentFragment(_)})

  def declarationFragment = parser("<%@", "%>", {CommentFragment(_)})

  def expressionFragment = parser("<%=", "%>", {ExpressionFragment(_)})

  def scriptletFragment = parser("<%", "%>", {ScriptletFragment(_)})

  def parser(prefix: String, postfix: String, transform: String => PageFragment) = {
    //val filler = """(.|\n|\r)+"""
    val filler = """.+"""
    val regex = (regexEscape(prefix) + filler + regexEscape(postfix)).r

    //prefix ~> rep(not(postfix)) <~ prefix ^^ {
    regex ^^ {
      case r => val text = r.toString
      val remaining = text.substring(prefix.length, text.length - postfix.length)
      transform(remaining)
    }
  }


  def regexEscape(text: String) = text.mkString("\\", "\\", "")


  def code = chunkOfText

  def any = chunkOfText

  //def chunkOfText = """.[^\<\$\%\-\}]*""".r
  def upToSpecialCharacter = """.[^\<\$\%\-\}]*""".r

  def chunkOfText = """.+""".r

  def token = """[a-zA-Z0-9\$_]+""".r

  def lessThanPercent = """\<\%""".r

  def percentGreaterThan = """\%\>""".r
}
