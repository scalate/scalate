package org.fusesource.scalate.mustache

import util.parsing.combinator.RegexParsers
import util.parsing.input.{Positional, CharSequenceReader, NoPosition, Position}
import org.fusesource.scalate.{TemplateException}

sealed abstract class Statement extends Positional {
}

case class Text(value: String) extends Statement {
  def +(other: String) = Text(value + other).setPos(pos)

  def +(other: Text) = Text(value + other.value).setPos(pos)

  def replaceAll(x: String, y: String) = Text(value.replaceAll(x, y)).setPos(pos)

  override def toString = value
}

case class Comment(comment: String) extends Statement
case class Variable(name: String, unescape: Boolean = false) extends Statement
case class InvertVariable(name: String) extends Statement
case class TagOpen(name: String) extends Statement
case class TagClose(name: String) extends Statement
case class Partial(name: String) extends Statement
case class SetDelimiter(open: String, close: String) extends Statement


/**
 * Parser for the Mustache template language
 *
 * @version $Revision : 1.1 $
 */
class MustacheParser extends RegexParsers {
  var open: String = "{{"
  var close: String = "}}"

  def parse(in: String) = {
    phrase(mustache)(new CharSequenceReader(in)) match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidTemplateException(message, next.pos);
    }
  }


  // Grammar
  //-------------------------------------------------------------------------
  def mustache = rep(expression | someText)

  def someText = upto(expression)

  // prefixed(open ~ opt(whiteSpace), (statement) <~ (opt(whiteSpace) ~ close))
  def expression = (open ~> statement <~ close) ^^ {
    case a: SetDelimiter =>
      open = a.open
      close = a.close
      println("applying new delim '" + a)
      a
    case s => s
  }

  def statement = unescapeVariable | invertVariable | partial | tagOpen | tagClose | comment | setDelimiter | variable

  def unescapeVariable = nameOperation("&") ^^ {Variable(_, true)}

  def tagOpen = nameOperation("#") ^^ {TagOpen(_)}

  def tagClose = nameOperation("/") ^^ {TagClose(_)}

  def invertVariable = nameOperation("^") ^^ {InvertVariable(_)}

  def partial = nameOperation(">") ^^ {Partial(_)}

  def comment = nameOperation("!") ^^ {Comment(_)}

  def variable = opt(whiteSpace) ~> name <~ opt(whiteSpace) ^^ {Variable(_, false)}

  def setDelimiter = ("=" ~> """\S+""".r <~ " ") ~ (upto("=" ~ close) <~ ("=")) ^^ {
    case a ~ b =>
      SetDelimiter(a, b.toString)
  }


  //def fail = """.+""".r ^^ { t => throw new InvalidSyntaxException(t) }
  //def badExpression = """.+|\z|$""".r ^^ {t => Text(open + t)}

  // Helper methods
  //-------------------------------------------------------------------------
  override def skipWhitespace = false

  def nameOperation(token: String) = (opt(whiteSpace) ~ token ~ opt(whiteSpace)) ~> name <~ opt(whiteSpace)

  val name = """\w+""".r

  def text(p1: Parser[String]): Parser[Text] = {
    positioned(p1 ^^ {Text(_)})
  }

  def upto[T](p: Parser[T]): Parser[Text] = text("""\z""".r) ~ failure("end of file") ^^ {null} |
          guard(p) ^^ {_ => Text("")} |
          rep1(not(p) ~> ".|\r|\n".r) ^^ {t => Text(t.mkString(""))}

  /**Once p1 is matched, disable backtracking. Consumes p1 and yields the result of p2 */
  def prefixed[T, U](p1: Parser[T], p2: Parser[U]) = p1.~!(p2) ^^ {case _ ~ x => x}

  /**Once p1 is matched, disable backtracking. Does not consume p1 and yields the result of p2 */
/*
  def guarded[T, U](p1: Parser[T], p2: Parser[U]) = guard(p1) ~! p2 ^^ {case _ ~ x => x}


  def wrapped[T, U](prefix: Parser[T], postfix: Parser[U]): Parser[Text] = {
    prefixed(prefix, upto(postfix) <~ postfix)
  }
*/

}

class InvalidTemplateException(val brief: String, val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos)