package org.fusesource.scalate.tool.commands

import org.fusesource.scalate.InvalidSyntaxException
import util.parsing.input.CharSequenceReader
import org.fusesource.scalate.support.{ Text => SSPText, ScalaParseSupport }
import org.fusesource.scalate.ssp._

/* an even simpler ssp parser */
class SspParser extends ScalaParseSupport {

  var skipWhitespaceOn = false

  override def skipWhitespace = skipWhitespaceOn

  def skip_whitespace[T](p: => Parser[T]): Parser[T] = Parser[T] {
    in =>
      skipWhitespaceOn = true
      val result = p(in)
      skipWhitespaceOn = false
      result
  }

  val anySpace = text("""[ \t]*""".r)
  val identifier = text("""[a-zA-Z0-9\$_]+""".r)
  val typeName = text(scalaType)
  val someText = text(""".+""".r)

  val attribute = skip_whitespace(opt(text("import")) ~ text("var" | "val") ~ identifier ~ (":" ~> typeName)) ~ ("""\s*""".r ~> opt("""=\s*""".r ~> upto("""\s*%>""".r))) ^^ {
    case (p_import ~ p_kind ~ p_name ~ p_type) ~ p_default => ScriptletFragment(p_kind + " " + p_name + ":" + p_type + " //attribute")
  }

  val literalPart: Parser[SSPText] =
    upto("<%" | """\<%""" | """\\<%""" | "${" | """\${""" | """\\${""" | """\#""" | """\\#""" | directives) ~
      opt(
        """\<%""" ~ opt(literalPart) ^^ { case x ~ y => "<%" + y.getOrElse("") } |
          """\${""" ~ opt(literalPart) ^^ { case x ~ y => "${" + y.getOrElse("") } |
          """\#""" ~ opt(literalPart) ^^ { case x ~ y => "#" + y.getOrElse("") } |
          """\\""" ^^ { s => """\""" }
      ) ^^ {
          case x ~ Some(y) => x + y
          case x ~ None => x
        }

  val tagEnding = "+%>" | """%>[ \t]*\r?\n""".r | "%>"
  val commentFragment = wrapped("<%--", "--%>") ^^ { CommentFragment(_) }
  val altCommentFragment = wrapped("<%#", "%>") ^^ { CommentFragment(_) }
  val dollarExpressionFragment = wrapped("${", "}") ^^ { ExpressionFragment(_) }
  val expressionFragment = wrapped("<%=", "%>") ^^ { ExpressionFragment(_) }
  val attributeFragement = prefixed("<%@", attribute <~ anySpace ~ tagEnding)
  val scriptletFragment = wrapped("<%", tagEnding) ^^ { ScriptletFragment(_) }
  val textFragment = literalPart ^^ { TextFragment(_) }

  def directives = ("#" ~> identifier ~ anySpace ~ opt("(" ~> scalaExpression <~ ")")) ^^ {
    case a ~ b ~ c => ScriptletFragment(a + c.map("(" + _ + ")").getOrElse(""))
  } | "#(" ~> identifier <~ ")" ^^ { ScriptletFragment(_) }

  def scalaExpression: Parser[SSPText] = {
    text(
      (rep(nonParenText) ~ opt("(" ~> scalaExpression <~ ")") ~ rep(nonParenText)) ^^ {
        case a ~ b ~ c =>
          val mid = b match {
            case Some(tb) => "(" + tb + ")"
            case tb => ""
          }
          a.mkString("") + mid + c.mkString("")
      }
    )
  }

  val nonParenText = characterLiteral | stringLiteral | """[^\(\)\'\"]+""".r

  val pageFragment: Parser[PageFragment] = directives | commentFragment | altCommentFragment | dollarExpressionFragment |
    attributeFragement | expressionFragment | scriptletFragment |
    textFragment

  val pageFragments = rep(pageFragment)

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    val x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

  def getPageFragments(in: String): List[PageFragment] = {
    phraseOrFail(pageFragments, in)
  }

}
