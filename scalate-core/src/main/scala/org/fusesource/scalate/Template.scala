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
import javax.servlet.http._
import org.fusesource.scalate.util.{Lazy, XmlEscape}
import java.text.{DateFormat, NumberFormat}
import java.util.{Date, Locale}
import java.io._
import javax.servlet.{ServletOutputStream, ServletContext, RequestDispatcher, ServletException}
import java.lang.String
import collection.mutable.{Stack, ListBuffer, HashMap}

class NoSuchAttributeException(val attribute: String) extends ServletException("No attribute called '" + attribute + "' was available in this SSP Template") {
}


class NoSuchTemplateException(val model: AnyRef, val view: String) extends ServletException("No '" + view + "' view template could be found for model object '" + model + "' of type: " + model.getClass.getCanonicalName) {
}

/**
 * The TemplateContext provides helper methods for interacting with the request, response, attributes and parameters
 */
case class TemplateContext(var out: PrintWriter, request: HttpServletRequest, response: HttpServletResponse, servletContext: ServletContext) {
  private val resourceBeanAttribute = "it"
  private val outStack = new Stack[PrintWriter]

  var nullString = ""
  var viewPrefixes = List("WEB-INF", "")
  var viewPostfixes = List(".ssp")
  val defaultCharacterEncoding = "ISO-8859-1";


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
   * Returns the attribute of the given type or a              { @link NoSuchAttributeException } exception is thrown
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
   * Returns the JAXRS resource bean of the given type or a             { @link NoSuchAttributeException } exception is thrown
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
   * Includes the given page inside this page
   */
  def include(page: String): Unit = {
    val dispatcher = getRequestDispatcher(page)
    doInclude(dispatcher)
  }

  /**
   * Forwards this request to the given page
   */
  def forward(page: String): Unit = {
    getRequestDispatcher(page).forward(request, response)
  }

  private def getRequestDispatcher(path: String) = {
    val dispatcher = request.getRequestDispatcher(path)
    if (dispatcher == null) {
      throw new ServletException("No dispatcher available for path: " + path)
    }
    else {
      dispatcher
    }
  }

  class RequestWrapper(request: HttpServletRequest) extends HttpServletRequestWrapper(request) {
    override def getMethod() = {
      "GET";
    }

    val _attributes = new HashMap[String, Object]

    override def setAttribute(name: String, value: Object) = _attributes(name) = value

    override def getAttribute(name: String) = _attributes.get(name).getOrElse(super.getAttribute(name))
  }

  class ResponseWrapper(response: HttpServletResponse, charEncoding: String = null) extends HttpServletResponseWrapper(response) {
    val sw = new StringWriter()
    val bos = new ByteArrayOutputStream()
    val sos = new ServletOutputStream() {
      def write(b: Int): Unit = {
        bos.write(b)
      }
    }
    var isWriterUsed = false
    var isStreamUsed = false
    var _status = 200


    override def getWriter(): PrintWriter = {
      if (isStreamUsed)
        throw new IllegalStateException("Attempt to import illegal Writer")
      isWriterUsed = true
      new PrintWriter(sw)
    }

    override def getOutputStream(): ServletOutputStream = {
      if (isWriterUsed)
        throw new IllegalStateException("Attempt to import illegal OutputStream")
      isStreamUsed = true
      sos
    }

    override def reset = {}

    override def resetBuffer = {}

    override def setContentType(x: String) = {} // ignore

    override def setLocale(x: Locale) = {} // ignore

    override def setStatus(status: Int): Unit = _status = status

    def getStatus() = _status

    def getString() = {
      if (isWriterUsed)
        sw.toString()
      else if (isStreamUsed) {
        if (charEncoding != null && !charEncoding.equals(""))
          bos.toString(charEncoding)
        else
          bos.toString(defaultCharacterEncoding)
      } else
        "" // target didn't write anything
    }
  }


  /**
   * Renders the view of the given model object, looking for the view in
   * packageName/className.viewName.ssp
   */
  def render(model: AnyRef, view: String = "index"): Unit= {
    if (model == null) {
      throw new NullPointerException("No model object given!")
    }
    var flag = true
    var aClass = model.getClass
    while (flag && aClass != null && aClass != classOf[Object]) {

      resolveViewForType(model, view, aClass) match {
        case Some(dispatcher) =>
          flag = false
          doInclude(dispatcher, model)

        case _ => aClass = aClass.getSuperclass
      }
    }

    if (flag) {
      aClass = model.getClass
      val interfaceList = new ListBuffer[Class[_]]()
      while (aClass != null && aClass != classOf[Object]) {
        for (i <- aClass.getInterfaces) {
          if (!interfaceList.contains(i)) {
            interfaceList.append(i)
          }
        }
        aClass = aClass.getSuperclass
      }

      flag = true
      for (i <- interfaceList; if (flag)) {
        resolveViewForType(model, view, i) match {
          case Some(dispatcher) =>
            flag = false
            doInclude(dispatcher, model)

          case _ =>
        }
      }
    }

    if (flag) {
      throw new NoSuchTemplateException(model, view)
    }
  }

  /**
   * Renders a collection of model objects with an optional separator
   */
  def renderCollection(objects: Traversable[AnyRef], view: String = "index", separator: ()=> String = {() => ""}): Unit= {
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


  protected def doInclude(dispatcher: RequestDispatcher, model: AnyRef = null): Unit = {
    out.flush

    val wrappedRequest = new RequestWrapper(request)
    val wrappedResponse = new ResponseWrapper(response)
    if (model != null) {
      wrappedRequest.setAttribute("it", model)
    }

    dispatcher.forward(wrappedRequest, wrappedResponse)
    val text = wrappedResponse.getString
    out.write(text)
    out.flush()
  }

  private def resolveViewForType(model: AnyRef, view: String, aClass: Class[_]): Option[RequestDispatcher] = {
    for (prefix <- viewPrefixes; postfix <- viewPostfixes) {
      val path = aClass.getName.replace('.', '/') + "." + view + postfix
      val fullPath = if (prefix.isEmpty) {"/" + path} else {"/" + prefix + "/" + path}

      val url = servletContext.getResource(fullPath)
      if (url != null) {
        val dispatcher = request.getRequestDispatcher(fullPath)
        if (dispatcher != null) {
          return Some(dispatcher)
          //return Some(new RequestDispatcherWrapper(dispatcher, fullPath, hc, model))
        }
      }
    }
    None
  }


  /**
   * Converts the value to a string so it can be output on the screen, which uses the             { @link # nullString } value
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
   * Converts the value to a string so it can be output on the screen, which uses the             { @link # nullString } value
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
   * Converts the value to an XML escaped string; a             { @link Seq[Node] } or             { @link Node } is passed through as is.
   * A null value uses the             { @link # nullString } value to display nulls
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


  // tag related stuff such as capturing blocks of output

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def evaluate(body: => Unit) : String = {
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

  
  implicit def body(body: => Unit) : () => String = {
    () => {
      evaluate(body)
    }
  }


  /**
   * Allow the right hand side to be written to the stream which makes it easy to code
   * generate expressions using blocks in the SSP code
   */
  def <<(value: Any) : Unit = {
    write(value)
  }

  /**
   * Like << but XML escapes the right hand side
   */
  def <<<(value: Any) : Unit = {
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
 * Defines a bunch of helper methods available to SSP pages
 *
 * @version $Revision : 1.1 $
 */
trait Template {
  def renderPage(context: TemplateContext): Unit
}