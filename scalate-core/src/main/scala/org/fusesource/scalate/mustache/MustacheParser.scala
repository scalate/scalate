package org.fusesource.scalate.mustache

import util.parsing.combinator.RegexParsers
import util.parsing.input.{Positional, CharSequenceReader, NoPosition, Position}
import org.fusesource.scalate.{InvalidSyntaxException, TemplateException}

sealed abstract class Statement extends Positional {
}

/**
 * Is a String with positioning information
 */
case class Text(value: String) extends Statement {
  def +(other: String) = Text(value + other).setPos(pos)
  def +(other: Text) = Text(value + other.value).setPos(pos)
  def replaceAll(x: String, y: String) = Text(value.replaceAll(x, y)).setPos(pos)
  override def toString = value
}

case class Comment(comment: Text) extends Statement
case class Variable(name: Text, unescape: Boolean = false) extends Statement
case class InvertVariable(name: Text) extends Statement
case class Tag(name: Text, body:List[Statement]) extends Statement
case class Partial(name: Text) extends Statement
case class SetDelimiter(open: Text, close: Text) extends Statement


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
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }


  // Grammar
  //-------------------------------------------------------------------------
  def mustache:Parser[List[Statement]] = rep(statement | someText)

  def someText = upto(open)

  def statement =  guarded(open, unescapeVariable | invertVariable | partial | tag | comment | setDelimiter | variable |
          failure("invalid statement"))

  def unescapeVariable = unescapeVariableAmp | unescapeVariableMustash
  def unescapeVariableAmp = expression(operation("&") ^^ {Variable(_, true)})
  def unescapeVariableMustash = expression("{"~>trimmed <~ "}" ^^ {Variable(_, true)})

  def tag = expression(operation("#") ^^ {case x=> Text(x.value) })  >> {
    case name =>
        mustache <~ expression(trim("/")~>trim(text(name.value))) ^^ {
        case body=> Tag(name, body)
      }  | error("Missing end tag '"+open+"/"+name+close+"' for started tag", name.pos)
  }

  def invertVariable = expression(operation("^") ^^ {InvertVariable(_)})

  def partial = expression(operation(">") ^^ {Partial(_)})

  def comment = expression(operation("!") ^^ {Comment(_)})

  def variable = expression(trimmed ^^ {Variable(_, false)})

  def setDelimiter = expression(("=" ~> text("""\S+""".r) <~ " ") ~ (upto("=" ~ close) <~ ("=")) ^^ {
    case a ~ b => SetDelimiter(a, b)
  }) ^^{
    case a =>
      open = a.open.value
      close = a.close.value
      println("applying new delim '" + a)
      a
  }

  // Helper methods
  //-------------------------------------------------------------------------
  override def skipWhitespace = false

  def operation(prefix:String):Parser[Text] = trim(prefix)~>trimmed
  def expression[T <: Statement](p:Parser[T]):Parser[T] = positioned(open ~> p <~ close)

  def trimmed:Parser[Text] = trim(text("""\w+""".r))
  def trim[T](p:Parser[T]=text("""\w+""".r)):Parser[T] = opt(whiteSpace) ~> p <~ opt(whiteSpace)

  def text(p1: Parser[String]): Parser[Text] = {
    positioned(p1 ^^ {Text(_)})
  }

  def upto[T](p: Parser[T]): Parser[Text] = text("""\z""".r) ~ failure("end of file") ^^ {null} |
          guard(p) ^^ {_ => Text("")} |
          rep1(not(p) ~> ".|\r|\n".r) ^^ {t => Text(t.mkString(""))}


  /**Once p1 is matched, disable backtracking. Does not consume p1 and yields the result of p2 */
  def guarded[T, U](p1: Parser[T], p2: Parser[U]) = guard(p1) ~! p2 ^^ {case _ ~ x => x}

  def error(message:String, pos:Position) = {
    throw new InvalidSyntaxException(message, pos);
  }

}