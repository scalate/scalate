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

import org.fusesource.scalate.parsers
import org.fusesource.scalate.parsers.ssp.DollarExpressionFragment

import scala.util.parsing.input.{ CharSequenceReader, NoPosition, Position, Positional }

sealed abstract class PageFragment extends Positional {
}

case class QualifiedName(
  prefix: String,
  name: String) extends Positional {

  val qualifiedName = prefix + ":" + name

  override def toString = qualifiedName
}

case class Attribute(
  name: String,
  value: Expression) extends Positional

case class CommentFragment(
  comment: Text) extends PageFragment

case class DollarExpressionFragment(
  code: Text) extends PageFragment {

  val toScala = ExpressionLanguage.asScala(code.toString)

  override def toString = "${" + toScala + "}"
}

case class TextFragment(
  text: Text) extends PageFragment {

  override def toString = text.toString

}

case class Element(
  qname: QualifiedName,
  attributes: List[Attribute],
  body: List[PageFragment]) extends PageFragment {

  val qualifiedName = qname.qualifiedName

  lazy val attributeMap: Map[String, Expression] = Map(attributes.map(a => a.name -> a.value): _*)

  /**
   * Returns the mandatory expression for the given attribute name or throw an expression if its not found
   */
  def attribute(name: String): Expression = attributeMap.get(name) match {
    case Some(e) => e
    case _ => throw new IllegalArgumentException("No '" + name + "' attribute on tag " + this)
  }
}

/**
 * Parser of JSP for the purposes of transformation to Scalate; so its not perfect but gives folks a head start
 *
 * @version $Revision : 1.1 $
 */
class JspParser extends MarkupScanner {

  protected val expressionParser = new ExpressionParser

  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    val x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidJspException(message, next.pos);
    }
  }

  def parsePage(in: String) = {
    phraseOrFail(page, in)
  }

  def page = rep(pageFragment)

  val pageFragment: Parser[PageFragment] = positioned(markup | expression | textFragment)

  val textFragment = upto(markup | expression) ^^ { ssp.TextFragment(_) }

  def elementTextContent = someUpto(closeElement | markup | expression) ^^ { ssp.TextFragment(_) }

  def markup: Parser[PageFragment] = element | emptyElement

  def emptyElement = (openElement("/>")) ^^ {
    case q ~ al => Element(q, al, Nil)
  }

  def element = (openElement(">") ~ rep(markup | expression | elementTextContent) ~ closeElement) ^^ {
    case (q ~ al) ~ b ~ q2 =>
      if (q != q2) throw new InvalidJspException("Expected close element of " + q + " but found " + q2, q2.pos)
      Element(q, al, b)
  }

  def qualifiedName: Parser[QualifiedName] = positioned(((IDENT <~ ":") ~ IDENT) ^^ { case p ~ n => QualifiedName(p, n) })

  def openElement(end: String) = "<" ~> qualifiedName ~ attributeList <~ end

  def closeElement: Parser[QualifiedName] = ("</" ~> qualifiedName) <~ repS ~ ">"

  def attributeList = rep(attribute)

  def attribute = ((S ~> IDENT <~ repS <~ "=" <~ repS) ~ STRING) ^^ { case n ~ v => parsers.Attribute(n, toExpression(v)) }

  val expression = wrapped("${", "}") ^^ { DollarExpressionFragment(_) }

  def toExpression(text: String) = expressionParser.parseExpression(text)
}

class InvalidJspException(
  val brief: String,
  val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos)
