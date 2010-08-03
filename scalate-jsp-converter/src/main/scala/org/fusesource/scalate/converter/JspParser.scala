package org.fusesource.scalate.converter

import org.fusesource.scalate.TemplateException
import util.parsing.input.{Positional, CharSequenceReader, NoPosition, Position}
import org.fusesource.scalate.support.Text

sealed abstract class PageFragment extends Positional {
}

case class QualifiedName(prefix: String, name: String) extends Positional {
  val qualifiedName = prefix + ":" + name

  override def toString = qualifiedName
}

case class Attribute(name: String, value: Expression) extends Positional

case class CommentFragment(comment: Text) extends PageFragment
case class DollarExpressionFragment(code: Text) extends PageFragment

case class TextFragment(text: Text) extends PageFragment {
  override def toString = text.toString
}

case class Element(qname: QualifiedName, attributes: List[Attribute], body: List[PageFragment]) extends PageFragment {
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
    var x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidJspException(message, next.pos);
    }
  }

  def parsePage(in: String) = {
    phraseOrFail(page, in)
  }


  def page = rep(pageFragment)

  val pageFragment: Parser[PageFragment] = positioned(markup | textFragment)

  val textFragment = upto(markup) ^^ {TextFragment(_)}

  def elementTextContent = someUpto(closeElement | markup) ^^ { TextFragment(_) }

  def markup: Parser[PageFragment] = element | emptyElement

  def emptyElement = (openElement("/>")) ^^ {
    case q ~ al => Element(q, al, Nil)
  }

  def element = (openElement(">") ~ rep(markup | elementTextContent) ~ closeElement) ^^{
    case (q ~ al) ~ b ~ q2 =>
      if (q != q2) throw new InvalidJspException("Expected close element of " + q + " but found " + q2, q2.pos)
      Element(q, al, b)}

  def qualifiedName = positioned(((IDENT <~ ":") ~ IDENT) ^^ {case p ~ n => QualifiedName(p, n)})

  def openElement(end:String) = "<" ~> qualifiedName ~ attributeList <~ end

  def closeElement: Parser[QualifiedName] = ("</" ~> qualifiedName) <~ repS ~ ">"

  def attributeList = rep(attribute)

  def attribute = ((S ~> IDENT <~ repS <~ "=" <~ repS) ~ STRING) ^^ {case n ~ v => Attribute(n, toExpression(v))}

  def toExpression(text: String) = expressionParser.parseExpression(text)
}

class InvalidJspException(val brief: String, val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos)