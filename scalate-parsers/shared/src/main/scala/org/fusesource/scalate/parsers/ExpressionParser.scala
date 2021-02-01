/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.parsers

import scala.util.parsing.input.{ CharSequenceReader, Positional }

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

/**
 * Parser for the JSTL EL expressions
 */
class ExpressionParser extends MarkupScanner {

  override def skipWhitespace = false

  def parseExpression(in: String): Expression = toExpression(phraseOrFail(expressionList, in))

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    val x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

  def toExpression(list: List[Expression]): Expression = {
    if (list.size == 1) {
      list(0)
    } else { CompositeExpression(list) }
  }

  // grammar
  //-------------------------------------------------------------------------

  def expressionList = rep(dollarExpression | staticText)

  def staticText = someUpto("${") ^^ { TextExpression(_) }

  val dollarExpression = wrapped("${", "}") ^^ { DollarExpression(_) }

}
