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
import org.fusesource.scalate.InvalidSyntaxException
import util.parsing.input.{Positional, CharSequenceReader}

sealed abstract class PageFragment extends Positional

case class Text(value:String) extends Positional {
  def +(other:String) = Text(value+other).setPos(pos)
  def +(other:Text) = Text(value+other.value).setPos(pos)
  def replaceAll(x:String, y:String) = Text(value.replaceAll(x,y)).setPos(pos)
  override def toString = value
}

case class CommentFragment(comment: Text) extends PageFragment
case class DollarExpressionFragment(code: Text) extends PageFragment
case class ExpressionFragment(code: Text) extends PageFragment
case class ScriptletFragment(code: Text) extends PageFragment
case class TextFragment(text: Text) extends PageFragment
case class AttributeFragment(kind: Text, name: Text, className: Text, defaultValue: Option[Text], autoImport: Boolean) extends PageFragment

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

  def text(p1:Parser[String]): Parser[Text] = {
    positioned(p1 ^^ { Text(_) })
  }

  val any_space   = text("""[ \t]*""".r)
  val identifier  = text("""[a-zA-Z0-9\$_]+""".r)
  val typeName    = text("""[a-zA-Z0-9\$_\[\]\.]+""".r)
  val some_text   = text(""".+""".r)

  val attribute = skip_whitespace(opt(text("import")) ~ text("var" | "val") ~ identifier ~ (":" ~> typeName)) ~ ("""\s*""".r ~> opt("""=\s*""".r ~> upto("""\s*%>""".r))) ^^ {
    case (p_import ~ p_kind ~ p_name ~ p_type) ~ p_default => AttributeFragment(p_kind, p_name, p_type, p_default, p_import.isDefined)
  }

  /**Once p1 is matched, disable backtracking. Consumes p1 and yields the result of p2 */
  def prefixed[T, U](p1: Parser[T], p2: Parser[U]) = p1.~!(p2) ^^ {case _ ~ x => x}

  /**Once p1 is matched, disable backtracking. Does not consume p1 and yields the result of p2 */
  def guarded[T, U](p1: Parser[T], p2: Parser[U]) = guard(p1) ~! p2 ^^ {case _ ~ x => x}

  def upto[T](p1: Parser[T]): Parser[Text] = {
    text(rep1(not(p1) ~> ".|\r|\n".r) ^^ {_.mkString("")})
  }

  def wrapped[T, U](prefix: Parser[T], postfix: Parser[U]): Parser[Text] = {
    prefixed(prefix, upto(postfix) <~ postfix)
  }

  def wrapped_end_guard[T, U](prefix: Parser[T], postfix: Parser[U]): Parser[Text] = {
    prefixed(prefix, upto(postfix))
  }

  val litteral_part:Parser[Text] =
    upto("<%" | """\<%""" | """\\<%""" | "${" | """\${""" | """\\${""" ) ~
      opt(
        """\<%""" ~ opt(litteral_part) ^^ { case x~y=> "<%"+y.getOrElse("") }  |
        """\${""" ~ opt(litteral_part) ^^ { case x~y=> "${"+y.getOrElse("") }  |
        """\\""" ^^ { s=>"""\""" }
      ) ^^ {
        case x~Some(y) => x+y
        case x~None => x
      }


  val comment_fragment = wrapped("<%--", "--%>") ^^ {CommentFragment(_)}
  val dollar_expression_fragment = wrapped("${", "}") ^^ {DollarExpressionFragment(_)}
  val expression_fragment =
    wrapped_end_guard("<%=", "-?%>".r) <~ ("""-%>[ \t]*\r?\n?""".r |"%>") ^^ {ExpressionFragment(_)}
  val attribute_fragement = prefixed("<%@", attribute <~ any_space ~ "%>")
  val scriptlet_fragment =
    wrapped_end_guard("<%", "-?%>".r) <~ ("""-%>[ \t]*\r?\n?""".r |"%>") ^^ {ScriptletFragment(_)}
  val text_fragment = litteral_part       ^^ { TextFragment(_) }

  val page_fragment: Parser[PageFragment] = positioned(comment_fragment | dollar_expression_fragment |
          attribute_fragement | expression_fragment | scriptlet_fragment |
          text_fragment)

  val page_fragments = rep(page_fragment)

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    var x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

  def getPageFragments(in: String): List[PageFragment] = {
    phraseOrFail(page_fragments, in)
  }

}
