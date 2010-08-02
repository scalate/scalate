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

package org.fusesource.scalate.ssp

import org.fusesource.scalate.InvalidSyntaxException
import util.parsing.input.{Positional, CharSequenceReader}
import org.fusesource.scalate.support.{Text, ScalaParseSupport}

sealed abstract class PageFragment extends Positional {
  def tokenName = toString
}

case class CommentFragment(comment: Text) extends PageFragment
case class DollarExpressionFragment(code: Text) extends PageFragment
case class ExpressionFragment(code: Text) extends PageFragment
case class ScriptletFragment(code: Text) extends PageFragment
case class TextFragment(text: Text) extends PageFragment
case class AttributeFragment(kind: Text, name: Text, className: Text, defaultValue: Option[Text], autoImport: Boolean) extends PageFragment

abstract class Directive(override val tokenName: String) extends PageFragment

case class IfFragment(code: Text) extends Directive("#if")
case class ElseIfFragment(code: Text) extends Directive("#elseif")
case class ElseFragment() extends Directive("#else")

case class MatchFragment(code: Text) extends Directive("#match")
case class CaseFragment(code: Text) extends Directive("#case")
case class OtherwiseFragment() extends Directive("#otherwise")

case class ForFragment(code: Text) extends Directive("#for")
case class DoFragment(code: Text) extends Directive("#do")
case class ImportFragment(code: Text) extends Directive("#import")
case class EndFragment() extends Directive("#end")

/**
 * Parser for the SSP template language 
 */
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
    case (p_import ~ p_kind ~ p_name ~ p_type) ~ p_default => AttributeFragment(p_kind, p_name, p_type, p_default, p_import.isDefined)
  }

  val literalPart: Parser[Text] =
  upto("<%" | """\<%""" | """\\<%""" | "${" | """\${""" | """\\${""" | """\#""" | """\\#""" | directives) ~
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
  val dollarExpressionFragment = wrapped("${", "}") ^^ {DollarExpressionFragment(_)}
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

}

