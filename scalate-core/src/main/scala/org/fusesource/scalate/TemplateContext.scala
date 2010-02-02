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
class DefaultRenderContext(var out: PrintWriter) extends RenderContext {
  
  /////////////////////////////////////////////////////////////////////
  //
  // RenderContext implementation
  //
  /////////////////////////////////////////////////////////////////////

  def <<(value: Any): Unit = {
    write(value)
  }

  def <<<(value: Any): Unit = {
    writeXmlEscape(value)
  }

  def binding(name: String) = {
    attributes.get(name)
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

  private val outStack = new Stack[PrintWriter]

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def capture(body: => Unit): String = {
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      body
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }


  /////////////////////////////////////////////////////////////////////
  //
  // attribute helpers/accessors
  //
  /////////////////////////////////////////////////////////////////////

  val attributes = new HashMap[String, Any]()
  
  /**
   * Returns the attribute of the given type or a    { @link NoValueSetException } exception is thrown
   */
  def attribute[T](name: String): T = {
    val value = attributes.get(name)
    if (value.isDefined) {
      value.get.asInstanceOf[T]
    }
    else {
      throw new NoValueSetException(name)
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

  def setAttribute[T](name: String, value: T): Unit = {
    attributes(name) = value
  }


  /////////////////////////////////////////////////////////////////////
  //
  // resource helpers/accessors
  //
  /////////////////////////////////////////////////////////////////////

  private val resourceBeanAttribute = "it"

  /**
   * Returns the JAXRS resource bean of the given type or a                { @link NoValueSetException } exception is thrown
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


  /////////////////////////////////////////////////////////////////////
  //
  // custom object rendering
  //
  /////////////////////////////////////////////////////////////////////

  var nullString = ""
  private var _numberFormat = new Lazy(NumberFormat.getNumberInstance(locale))
  private var _percentFormat = new Lazy(NumberFormat.getPercentInstance(locale))
  private var _dateFormat = new Lazy(DateFormat.getDateInstance(DateFormat.FULL, locale))

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

}
