package org.fusesource.scalate.converter

import util.parsing.input.{Positional, CharSequenceReader}
import org.fusesource.scalate.support.Text
import org.fusesource.scalate.InvalidSyntaxException

object ExpressionLanguage {
  protected val operators = Map("eq" -> "==", "ne" -> "!=",
    "gt" -> ">", "ge" -> ">=",
    "lt" -> "<", "le" -> "<=")

  def asScala(el: String): String = {
    // lets switch the EL style indexing to Scala parens and switch single quotes to doubles
    var text = el.replace('[', '(').
            replace(']', ')').
            replace('\'', '\"')
    for ((a, b) <- operators) {
      text = text.replaceAll("\\s" + a + "\\s", " " + b + " ")
    }
    // lets convert the foo.bar into foo.getBar
    var first = true
    text.split('.').map(s =>
      if (!first && s.length > 0 && s(0).isUnicodeIdentifierStart) {
        "get" + s.capitalize
      } else {
        first = false
        s
      }).mkString(".")
  }
}
sealed abstract class Expression extends Positional {

  /**
   * Returns the text of an expression as a numeric method parameter
   */
  def asUnquotedParam: String

  /**
   * Returns the text of an expression as a method parameter quoting String values
   */
  def asParam: String

  /**
   * Returns the text of an expression as a method parameter
   */
  def asJsp: String

}

case class TextExpression(text: Text) extends Expression {
  def asUnquotedParam = text.toString

  def asParam = "\"" + text + "\""

  def asJsp = text.toString

}

case class CompositeExpression(list: List[Expression]) extends Expression {
  def asUnquotedParam = list.map(_.asUnquotedParam).mkString(" + ")

  def asParam = list.map(_.asParam).mkString(" + ")

  def asJsp = list.map(_.asJsp).mkString(" + ")
}

case class DollarExpression(code: Text) extends Expression {
  val toScala = ExpressionLanguage.asScala(code.toString)

  def asUnquotedParam = toScala

  def asParam = toScala

  def asJsp = "${" + toScala + "}"
}

/*
case class DollarExpression(list: List[ExpressionNode]) extends Expression {
  def code = list.map(_.toScala).mkString(" ")

  def asUnquotedParam = code

  def asParam = code

  def asJsp = "${" + code + "}"

}

sealed abstract class ExpressionNode {
  def toScala: String
}

case class TextNode(text: Text) extends ExpressionNode {
  def toScala = text.toString
}

case class ArrayNode(list: List[ExpressionNode]) extends ExpressionNode {
  def toScala = list.mkString("(", " ", ")")
}

case class PathNode(variable: String, name: String) extends ExpressionNode {
  def toScala = variable + "." + name
}
*/


/**
 * Parser for the JSTL EL expressions
 */
class ExpressionParser extends MarkupScanner {
  override def skipWhitespace = false


  def parseExpression(in: String): Expression = toExpression(phraseOrFail(expressionList, in))

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    var x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

  def toExpression(list: List[Expression]): Expression = {
    if (list.size == 1) {
      list(0)
    }
    else {CompositeExpression(list)}
  }

  // grammar
  //-------------------------------------------------------------------------

  def expressionList = rep(dollarExpression | staticText)

  def staticText = someUpto("${") ^^ {TextExpression(_)}

  val dollarExpression = wrapped("${", "}") ^^ {DollarExpression(_)}

  /*
    val dollarExpression = ("${" ~> expression("}") <~ "}") ^^ {DollarExpression(_)}

    def expression(term: String): Parser[List[ExpressionNode]] = rep(log(path | arrayAccess | word(term))("expression"))

    def word(term: String) = someUpto(term) ^^ {TextNode(_)}

    def path = (IDENT ~ ("." ~> IDENT)) ^^ {case a ~ b => PathNode(a, b)}

    def arrayAccess = "[" ~> expression("]") <~ "]" ^^ {ArrayNode(_)}

  */
}

