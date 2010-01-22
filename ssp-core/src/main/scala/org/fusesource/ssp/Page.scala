package org.fusesource.ssp

import scala.xml.Node
import javax.servlet.ServletException
import javax.servlet.http._
import java.io.PrintWriter
import org.fusesource.ssp.util.{Lazy, XmlEscape}
import java.text.{DateFormat, NumberFormat}
import java.util.{Date, Locale}

class NoSuchAttributeException(val attribute: String) extends ServletException("No attribute called '" + attribute + "' was available in this SSP Page") {
}

/**
 * The PageContext provides helper methods for interacting with the request, response, attributes and parameters
 */
case class PageContext(out: PrintWriter, request: HttpServletRequest, response: HttpServletResponse) {
  private val resourceBeanAttribute = "it"

  var nullString = ""

  private var _numberFormat = new Lazy(NumberFormat.getNumberInstance(locale))
  private var _percentFormat = new Lazy(NumberFormat.getPercentInstance(locale))
  private var _dateFormat = new Lazy(DateFormat.getDateInstance(DateFormat.FULL, locale))


  /**
   * Called after each page completes
   */
  def completed = {
    out.flush
  }

  /**
   * Returns the attribute of the given type or a        { @link NoSuchAttributeException } exception is thrown
   */
  def attribute[T](name: String): T = {
    val value = request.getAttribute(name)
    if (value != null) {
      value.asInstanceOf[T]
    }
    else {
      throw new NoSuchAttributeException(name)
    }
  }

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: T): T = {
    val value = request.getAttribute(name)
    if (value != null) {
      value.asInstanceOf[T]
    }
    else {
      defaultValue
    }
  }

  /**
   * Returns the JAXRS resource bean of the given type or a       { @link NoSuchAttributeException } exception is thrown
   */
  def resource[T]: T = {
    attribute[T](resourceBeanAttribute)
  }

  /**
   * Returns the JAXRS resource bean of the given type or the default value if it is not available
   */
  def resourceOrElse[T](defaultValue: T): T = {
    attributeOrElse(resourceBeanAttribute, defaultValue)
  }


  /**
   * Converts the value to a string so it can be output on the screen, which uses the       { @link # nullString } value
   * for nulls
   */
  def toString(value: Any): String = {
    value match {
      case d: Date => dateFormat.format(d)
      case n: Number => numberFormat.format(n)
      case a => if (a == null) {nullString} else {a.toString}
    }
  }

  /**
   * Converts the value to a string so it can be output on the screen, which uses the       { @link # nullString } value
   * for nulls
   */
  def write(value: Any): Unit = {
    value match {
      case n: Node => out.print(n)
      case s: Seq[Node] => for (n <- s) {out.print(n)}
      case a => out.print(toString(a))
    }
  }

  /**
   * Converts the value to an XML escaped string; a       { @link Seq[Node] } or       { @link Node } is passed through as is.
   * A null value uses the       { @link # nullString } value to display nulls
   */
  def writeXmlEscape(value: Any): Unit = {
    value match {
      case n: Node => out.print(n)
      case s: Seq[Node] => for (n <- s) {out.print(n)}
      case a => write(XmlEscape.escape(toString(a)))
    }
  }


  /**
   * Returns the formatted string using the locale of the users request or the default locale if not available
   */
  def format(pattern: String, args: AnyRef*) = {
    String.format(locale, pattern, args: _*)
  }

  def percent(number: Number) = percentFormat.format(number)

  // Locale based formatters
  //
  // shame we can't use 'lazy var' for this cruft...
  def numberFormat: NumberFormat = _numberFormat()

  def numberFormat_=(value: NumberFormat): Unit = _numberFormat(value)

  def percentFormat: NumberFormat = _percentFormat()

  def percentFormat_=(value: NumberFormat): Unit = _percentFormat(value)

  def dateFormat: DateFormat = _dateFormat()

  def dateFormat_=(value: DateFormat): Unit = _dateFormat(value)


  def locale: Locale = {
    var locale = request.getLocale
    if (locale == null) {
      Locale.getDefault
    }
    else {
      locale
    }
  }
}

/**
 * Defines a bunch of helper methods available to SSP pages
 *
 * @version $Revision : 1.1 $
 */
abstract class Page extends HttpServlet {
  def createPageContext(out: PrintWriter, request: HttpServletRequest, response: HttpServletResponse) = {
    PageContext(out, request, response)
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val out = response.getWriter
    val pageContext = createPageContext(out, request, response)
    render(pageContext)
  }

  def render(pageContext: PageContext): Unit

}