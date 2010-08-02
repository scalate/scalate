package org.fusesource.scalate.converter

import util.parsing.input.{Positional, CharSequenceReader}
import org.fusesource.scalate.support.{Text, ScalaParseSupport}
import org.fusesource.scalate.InvalidSyntaxException

sealed abstract class Expression extends Positional {
}

case class TextExpression(text: Text) extends Expression

case class CompositeExpression(list: List[Expression]) extends Expression
case class DollarExpression(code: Text) extends Expression

/**
 * Parser for the KS  { E: expressions
 */
class ExpressionParser extends ScalaParseSupport {
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

  
  def text(p1: Parser[String]): Parser[Text] = {
    positioned(p1 ^^ {Text(_)})
  }


  def upto[T](p1: Parser[T]): Parser[Text] = {
    val p = p1 | directives

    text(
      text("""\z""".r) ~ failure("end of file") ^^ {null} |
              guard(p) ^^ {_ => ""} |
              rep1(not(p) ~> ".|\r|\n".r) ^^ {_.mkString("")}
      )
  }

  def wrapped[T, U](prefix: Parser[T], postfix: Parser[U]): Parser[Text] = {
    prefixed(prefix, upto(postfix) <~ postfix)
  }

  val anySpace = text("""[ \t]*""".r)
  val identifier = text("""[a-zA-Z0-9\$_]+""".r)
  val typeName = text(scalaType)
  val someText = text(""".+""".r)

  val attribute = skip_whitespace(opt(text("import")) ~ text("var" | "val") ~ identifier ~ (":" ~> typeName)) ~ ("""\s*""".r ~> opt("""=\s*""".r ~> upto("""\s*%>""".r))) ^^ {
    case (p_import ~ p_kind ~ p_name ~ p_type) ~ p_default => AttributeFragment(p_kind, p_name, p_type, p_default, p_import.isDefined)
  }

  val literalPart: Parser[Text] =
  upto("<%" | """\<%""" | """\\<%""" | "${" | """\${""" | """\\${""" | """\#""" | """\\#""") ~
          opt(
            """\<%""" ~ opt(literalPart) ^^ {case x ~ y => "<%" + y.getOrElse("")} |
                    """\${""" ~ opt(literalPart) ^^ {case x ~ y => "${" + y.getOrElse("")} |
                    """\#""" ~ opt(literalPart) ^^ {case x ~ y => "#" + y.getOrElse("")} |
                    """\\""" ^^ {s => """\"""}
            ) ^^ {
    case x ~ Some(y) => x + y
    case x ~ None => x
  }

  val tagEnding = "+%>" | """%>[ \t]*\r?\n?""".r
  val commentFragment = wrapped("<%--", "--%>") ^^ {CommentFragment(_)}
  val expressionFragment = wrapped("<%=", tagEnding) ^^ {ExpressionFragment(_)}
  val attributeFragement = prefixed("<%@", attribute <~ anySpace ~ tagEnding)
  val scriptletFragment = wrapped("<%", tagEnding) ^^ {ScriptletFragment(_)}
  val textFragment = literalPart ^^ {TextFragment(_)}

  val pageFragment: Parser[PageFragment] = positioned(directives | commentFragment | dollarExpressionFragment |
          attributeFragement | expressionFragment | scriptletFragment |
          textFragment)

  val pageFragments = rep(pageFragment)


  def directives: Parser[PageFragment] = ifExpression | elseIfExpression | elseExpression |
          matchExpression | caseExpression | otherwiseExpression |
          forExpression | doExpression | velocityScriplet | importExpression | endExpression

  // if / elseif / else
  def ifExpression = expressionDirective("if") ^^ {IfFragment(_)}

  def elseIfExpression = expressionDirective("elseif" | "elif") ^^ {ElseIfFragment(_)}

  def elseExpression = emptyDirective("else") ^^ {case a => ElseFragment()}

  // match / case / otherwise
  def matchExpression = expressionDirective("match") ^^ {MatchFragment(_)}

  def caseExpression = expressionDirective("case") ^^ {CaseFragment(_)}

  def otherwiseExpression = emptyDirective("otherwise") ^^ {case a => OtherwiseFragment()}


  // other directives
  def velocityScriplet = wrapped("#{", "}#") ^^ {ScriptletFragment(_)}

  def forExpression = expressionDirective("for" ~ opt("each")) ^^ {ForFragment(_)}

  def doExpression = expressionDirective("do") ^^ {DoFragment(_)}

  def importExpression = expressionDirective("import") ^^ {ImportFragment(_)}

  def endExpression = emptyDirective("end") ^^ {case a => EndFragment()}

  // useful for implementing directives
  def emptyDirective(name: String) = text(("#" + name) | ("#(" + name + ")"))

  def expressionDirective(name: String) = ("#" ~ name ~ anySpace ~ "(") ~> scalaExpression <~ ")"

  def expressionDirective[T](p: Parser[T]) = ("#" ~ p ~ anySpace ~ "(") ~> scalaExpression <~ ")"

  def scalaExpression: Parser[Text] = {
    text(
      (rep(nonParenText) ~ opt("(" ~> scalaExpression <~ ")") ~ rep(nonParenText)) ^^ {
        case a ~ b ~ c =>
          val mid = b match {
            case Some(tb) => "(" + tb + ")"
            case tb => ""
          }
          a.mkString("") + mid + c.mkString("")
      })
  }

  val nonParenText = characterLiteral | stringLiteral | """[^\(\)\'\"]+""".r

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    var x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

  def getPageFragments(in: String): List[PageFragment] = {
    phraseOrFail(pageFragments, in)
  }
  */
}

