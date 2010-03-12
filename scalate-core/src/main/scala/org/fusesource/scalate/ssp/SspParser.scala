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
import util.parsing.input.CharSequenceReader
import org.fusesource.scalate.InvalidSyntaxException

sealed abstract class PageFragment

case class CommentFragment(comment: String) extends PageFragment
case class DollarExpressionFragment(code: String) extends PageFragment
case class ExpressionFragment(code: String) extends PageFragment
case class ScriptletFragment(code: String) extends PageFragment
case class TextFragment(text: String) extends PageFragment
case class AttributeFragment(kind: String, name: String, className: String, defaultValue: Option[String], autoImport: Boolean) extends PageFragment

class SspParser extends RegexParsers {
  var skipWhitespaceOn = false

  override def skipWhitespace = skipWhitespaceOn

  def skip_whitespace[T](p: => Parser[T]): Parser[T] = Parser[T] {
    in =>
      skipWhitespaceOn = true
      val result = p(in)
      skipWhitespaceOn = false
      result
  }

  val any_space = """[ \t]*""".r
  val identifier = """[a-zA-Z0-9\$_]+""".r
  val typeName = """[a-zA-Z0-9\$_\[\]\.]+""".r
  val some_text = """.+""".r

  val attribute = skip_whitespace(opt("import") ~ ("var" | "val") ~ identifier ~ (":" ~> typeName)) ~ opt("""\s*=\s*""".r ~> upto("""\s*%>""".r)) ^^ {
    case (p_import ~ p_kind ~ p_name ~ p_type) ~ p_default => AttributeFragment(p_kind, p_name, p_type, p_default, p_import.isDefined)
  }

  /**Once p1 is matched, disable backtracking. Consumes p1 and yields the result of p2 */
  def prefixed[T, U](p1: Parser[T], p2: Parser[U]) = p1.~!(p2) ^^ {case _ ~ x => x}

  /**Once p1 is matched, disable backtracking. Does not consume p1 and yields the result of p2 */
  def guarded[T, U](p1: Parser[T], p2: Parser[U]) = guard(p1) ~! p2 ^^ {case _ ~ x => x}

  def upto[T](p1: Parser[T]): Parser[String] = {
    rep1(not(p1) ~> ".|\r|\n".r) ^^ {_.mkString("")}
  }

  def wrapped[T, U](prefix: Parser[T], postfix: Parser[U]): Parser[String] = {
    prefixed(prefix, upto(postfix) <~ postfix)
  }


  val comment_fragment = wrapped("<%--", "--%>") ^^ {CommentFragment(_)}
  val dollar_expression_fragment = wrapped("${", "}") ^^ {DollarExpressionFragment(_)}
  val expression_fragment = wrapped("<%=", "%>") ^^ {ExpressionFragment(_)}
  val attribute_fragement = prefixed("<%@", attribute <~ any_space ~ "%>")
  val scriptlet_fragment = wrapped("<%", "%>") ^^ {ScriptletFragment(_)}

  //val text_fragment               = upto("<%" | "${")       ^^ { TextFragment(_) }
  val text_fragment: Parser[TextFragment] = {

    def textOf(optionalTextFragment: Option[TextFragment]) = optionalTextFragment match {
      case Some(textFragment) => textFragment.text
      case _ => ""
    }

    upto("<%" | """\<%""" | """${""" | """\${""") ~
            opt(
              """\${""" ~ opt(text_fragment) ^^ {case x ~ y => "${" + textOf(y)} |
                      """\<%""" ~ opt(text_fragment) ^^ {case x ~ y => "<%" + textOf(y)}
              ) ^^ {
      case x ~ Some(y) => TextFragment(x + y)
      case x ~ None => TextFragment(x)
    }
  }

  val any_space_then_nl = """[ \t]*\r?\n""".r


  val page_fragment: Parser[PageFragment] = comment_fragment | dollar_expression_fragment |
          attribute_fragement | expression_fragment | scriptlet_fragment |
          text_fragment

  val page_fragments = rep(page_fragment)

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    var x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case _ => throw new InvalidSyntaxException(x.toString)
    }
  }

  def getPageFragments(in: String): List[PageFragment] = {
    phraseOrFail(page_fragments, in)
  }

}
