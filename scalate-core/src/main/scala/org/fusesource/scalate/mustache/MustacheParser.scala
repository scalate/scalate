package org.fusesource.scalate.mustache

import util.parsing.combinator.RegexParsers
import util.parsing.input.{Positional, CharSequenceReader, Position}
import org.fusesource.scalate.{InvalidSyntaxException}
import org.fusesource.scalate.util.Logging

sealed abstract class Statement extends Positional {
}

/**
 * Is a String with positioning information
 */
case class Text(value: String) extends Statement {
  def +(other: String) = Text(value + other).setPos(pos)

  def +(other: Text) = Text(value + other.value).setPos(pos)

  def replaceAll(x: String, y: String) = Text(value.replaceAll(x, y)).setPos(pos)

  def isWhitespace: Boolean = value.trim.length == 0

  override def toString = value
}

case class Comment(comment: Text) extends Statement
case class Variable(name: Text, unescape: Boolean = false) extends Statement
case class Section(name: Text, body: List[Statement]) extends Statement
case class InvertSection(name: Text, body: List[Statement]) extends Statement
case class Partial(name: Text) extends Statement
case class SetDelimiter(open: Text, close: Text) extends Statement
case class ImplicitIterator(name: String) extends Statement
case class Pragma(name: Text, options: Map[String, String]) extends Statement


/**
 * Parser for the Mustache template language
 *
 * @version $Revision : 1.1 $
 */
class MustacheParser extends RegexParsers with Logging {
  var open: String = "{{"
  var close: String = "}}"

  def parse(in: String) = {
    phrase(mustache)(new CharSequenceReader(in)) match {
      case Success(s, _) => s
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }


  // Grammar
  //-------------------------------------------------------------------------
  def mustache: Parser[List[Statement]] = rep(statement | someText)

  def someText = upto(open)

  def statement = guarded(open, unescapeVariable | partial | pragma | section | invert | comment | setDelimiter | variable |
          failure("invalid statement"))

  def unescapeVariable = unescapeVariableAmp | unescapeVariableMustash

  def unescapeVariableAmp = expression(operation("&") ^^ {Variable(_, true)})

  def unescapeVariableMustash = expression("{" ~> trimmed <~ "}" ^^ {Variable(_, true)})

  def section = positioned(nested("#") ^^ {
    case (name, body) => Section(name, body)
  })

  def invert = positioned(nested("^") ^^ {
    case (name, body) => InvertSection(name, body)
  })

  def partial = expression(operation(">") ^^ {Partial(_)})

  def pragma = expression(operation("%") ~ rep(option) ^^ {
    case p ~ o =>
      val options = Map(o: _*)
      p match {
        case Text("IMPLICIT-ITERATOR") =>
          val name = options.getOrElse("iterator", ".")
          ImplicitIterator(name)
        case _ =>
          Pragma(p, options)
      }
  })

  def option = trimmed ~ ("=" ~> trimmed) ^^ {case n ~ v => n.value -> v.value}

  def comment = expression((trim("!") ~> upto(close)) ^^ {Comment(_)})

  def variable = expression(trimmed ^^ {Variable(_, false)})

  def setDelimiter = expression(("=" ~> text("""\S+""".r) <~ " ") ~ (upto("=" ~ close) <~ ("=")) ^^ {
    case a ~ b => SetDelimiter(a, b)
  }) <~ opt(whiteSpace) ^^ {
    case a =>
      open = a.open.value
      close = a.close.value
      debug("applying new delim '" + a)
      a
  }



  // Helper methods
  //-------------------------------------------------------------------------

  def operation(prefix: String): Parser[Text] = trim(prefix) ~> trimmed

  def nested(prefix: String): Parser[(Text, List[Statement])] = expression(operation(prefix) ^^ {case x => Text(x.value)}) >> {
    case name =>
      opt(whiteSpace) ~> mustache <~ expression(trim("/") ~> trim(text(name.value))) <~ opt(whiteSpace) ^^ {
        case body => (name, body)
      } | error("Missing end tag '" + open + "/" + name + close + "' for started tag", name.pos)
  }


  override def skipWhitespace = false

  def expression[T <: Statement](p: Parser[T]): Parser[T] = positioned(open ~> p <~ close)

  def trimmed = trim(text("""(\w|\.)[^\s={}]*""".r))

  def trim[T](p: Parser[T]): Parser[T] = opt(whiteSpace) ~> p <~ opt(whiteSpace)

  def text(p1: Parser[String]): Parser[Text] = {
    positioned(p1 ^^ {Text(_)})
  }

  def upto[T](p: Parser[T]): Parser[Text] = text("""\z""".r) ~ failure("end of file") ^^ {null} |
          guard(p) ^^ {_ => Text("")} |
          rep1(not(p) ~> ".|\r|\n".r) ^^ {t => Text(t.mkString(""))}


  /**Once p1 is matched, disable backtracking. Does not consume p1 and yields the result of p2 */
  def guarded[T, U](p1: Parser[T], p2: Parser[U]) = guard(p1) ~! p2 ^^ {case _ ~ x => x}

  def error(message: String, pos: Position) = {
    throw new InvalidSyntaxException(message, pos);
  }

  def isWhitespace(statement: Statement): Boolean = statement match {
    case t: Text => t.isWhitespace
    case _ => false
  }
}