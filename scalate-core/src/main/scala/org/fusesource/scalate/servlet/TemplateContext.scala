/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate


import scala.xml.Node
import org.fusesource.scalate.util.{Lazy, XmlEscape}
import java.text.{DateFormat, NumberFormat}
import java.util.{Date, Locale}
import java.io._
import java.lang.String
import collection.mutable.{Stack, ListBuffer, HashMap}

/**
 * The TemplateContext provides helper methods for interacting with the request, response, attributes and parameters
 */
abstract class TemplateContext extends RenderCollector {
  
  private val resourceBeanAttribute = "it"
  private val outStack = new Stack[PrintWriter]

  var nullString = ""
  var viewPrefixes = List("WEB-INF", "")
  var viewPostfixes = List(".ssp")
  val defaultCharacterEncoding = "ISO-8859-1";


  private var _numberFormat = new Lazy(NumberFormat.getNumberInstance(locale))
  private var _percentFormat = new Lazy(NumberFormat.getPercentInstance(locale))
  private var _dateFormat = new Lazy(DateFormat.getDateInstance(DateFormat.FULL, locale))


  def out: PrintWriter
  def out_=(out:PrintWriter): Unit

  /**
   * Called after each page completes
   */
  def completed = {
    out.flush
  }

  /**
   * Returns the attribute of the given type or a    { @link NoSuchAttributeException } exception is thrown
   */
  def attribute[T](name: String): T

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: T): T

  /**
   * Returns the JAXRS resource bean of the given type or a                { @link NoSuchAttributeException } exception is thrown
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


  // Rendering methods

  /**
   * Includes the given template inside this template
   */
  def include(page: String): Unit

  /**
   * Forwards this request to the given page
   */
  def forward(page: String): Unit


  /**
   * Renders a collection of model objects with an optional separator
   */
  def renderCollection(objects: Traversable[AnyRef], view: String = "index", separator: () => String = {() => ""}): Unit = {
    var first = true
    for (model <- objects) {
      if (first) {
        first = false
      }
      else {
        val text = separator()
        write(text)
      }
      render(model, view)
    }
  }

  def render(model: AnyRef, view: String = "index"): Unit


  /**
   * Converts the value to a string so it can be output on the screen, which uses the                { @link # nullString } value
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
   * Converts the value to a string so it can be output on the screen, which uses the                { @link # nullString } value
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
   * Converts the value to an XML escaped string; a                { @link Seq[Node] } or                { @link Node } is passed through as is.
   * A null value uses the                { @link # nullString } value to display nulls
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
    Locale.getDefault
  }


  // tag related stuff such as capturing blocks of output

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def evaluate(body: => Unit): String = {
    val buffer = new StringWriter();
    val printWriter = new PrintWriter(buffer)
    pushOut(printWriter)
    try {
      body
      printWriter.close()
      buffer.toString
    } finally {
      popOut
    }
  }

  def pushOut(newOut: PrintWriter) {
    outStack.push(out)
    out = newOut
  }

  def popOut() = {
    out = outStack.pop
  }


  implicit def body(body: => Unit): () => String = {
    () => {
      evaluate(body)
    }
  }


  /**
   * Allow the right hand side to be written to the stream which makes it easy to code
   * generate expressions using blocks in the SSP code
   */
  def <<(value: Any): Unit = {
    write(value)
  }

  /**
   * Like << but XML escapes the right hand side
   */
  def <<<(value: Any): Unit = {
    writeXmlEscape(value)
  }

  /*
    def textWrite_=(value: Any) : Unit = {
      write(value)
    }

    def xmlEscapedWrite_=(value: Any) : Unit = {
      writeXmlEscape(value)
    }
  */
}


/**
 * A default template context for use outside of servlet environments
 */
case class DefaultTemplateContext(var out: PrintWriter) extends TemplateContext() {
  val attributes = new HashMap[String, Any]()

  /**
   * Returns the attribute of the given type or a    { @link NoSuchAttributeException } exception is thrown
   */
  def attribute[T](name: String): T = {
    val value = attributes.get(name)
    if (value.isDefined) {
      value.get.asInstanceOf[T]
    }
    else {
      throw new NoSuchAttributeException(name)
    }
  }

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: T): T = {
    val value = attributes.get(name)
    if (value.isDefined) {
      value.get.asInstanceOf[T]
    }
    else {
      defaultValue
    }
  }

  def render(model: AnyRef, view: String = "index"): Unit = throw new UnsupportedOperationException()

  def include(page: String) = throw new UnsupportedOperationException()

  def forward(page: String) = throw new UnsupportedOperationException()
}
