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


import org.fusesource.scalate.util.{Lazy, RenderHelper}
import java.text.{DateFormat, NumberFormat}
import java.util.{Date, Locale}
import java.io._
import java.lang.String
import collection.mutable.{Stack, ListBuffer, HashMap}
import xml.{NodeBuffer, Node}

/**
 * The TemplateContext provides helper methods for interacting with the request, response, attributes and parameters
 */
class DefaultRenderContext(val engine: TemplateEngine, var out: PrintWriter) extends RenderContext {
  var viewPrefixes = List("")
  var viewPostfixes = engine.codeGenerators.keysIterator.map(x => "." + x).toList
  var currentTemplate: String = _
  private val _attributes: AttributeMap[String, Any] = new HashMap[String, Any]() with AttributeMap[String, Any]

  def attributes = _attributes

  /////////////////////////////////////////////////////////////////////
  //
  // RenderContext implementation
  //
  //////////////////////////////////x///////////////////////////////////

  def <<(value: Any): Unit = {
    out.print(toString(value))
  }

  def <<<(value: Any): Unit = {
    out.print(RenderHelper.sanitize(toString(value)))
  }

  def toString(value: Any): String = {
    value match {
      case u: Unit => ""
      case null => nullString
      case v: String => v
      case v: Date => dateFormat.format(v)
      case v: Number => numberFormat.format(v)
      case f: FilterRequest => {
        var rc = filter(f.filter, f.content)
        rc
      }
      case s: NodeBuffer =>
        (s.foldLeft(new StringBuilder) {(rc, x) => rc.append(x)}).toString

      // TODO for any should we use the renderView?
      case v: Any => v.toString
    }
  }

  def filter(name: String, content: String): String = {
    engine.filters.get(name) match {
      case None => throw new NoSuchFilterException(name)
      case Some(f) => f.filter(content)
    }
  }

  /**
   * includes the given template path applying the layout to it before returning
   */
  def layout(path: String): Unit = include(path, true)

  def include(path: String): Unit = include(path, false)

  def include(path: String, layout: Boolean): Unit = {
    val uri = resolveUri(path)

    withUri(uri) {
      val template = engine.load(uri)
      if (layout) {
        engine.layout(template, this);
      }
      else {
        template.render(this);
      }
    }
  }

  def renderCollection(objects: Traversable[AnyRef], view: String = "index", separator: () => String = {() => ""}): String = {
    capture {
      includeCollection(objects, view, separator)
    }
  }

  /**
   * Renders a collection of model objects with an optional separator
   */
  def includeCollection(objects: Traversable[AnyRef], view: String = "index", separator: () => String = {() => ""}): Unit = {
    var first = true
    for (model <- objects) {
      if (first) {
        first = false
      }
      else {
        this << separator()
      }
      includeView(model, view)
    }
  }

  def renderView(model: AnyRef, view: String = "index"): String = {
    capture {
      includeView(model, view)
    }
  }

  /**
   * Renders the view of the given model object, looking for the view in
   * packageName/className.viewName.ext
   */
  def includeView(model: AnyRef, view: String = "index"): Unit = {
    if (model == null) {
      throw new NullPointerException("No model object given!")
    }

    val classSearchList = new ListBuffer[Class[_]]()

    def buildClassList(clazz: Class[_]): Unit = {
      if (clazz != null && clazz != classOf[Object] && !classSearchList.contains(clazz)) {
        classSearchList.append(clazz);
        buildClassList(clazz.getSuperclass)
        for (i <- clazz.getInterfaces) {
          buildClassList(i)
        }
      }
    }

    def viewForClass(clazz: Class[_]): String = {
      for (prefix <- viewPrefixes; postfix <- viewPostfixes) {
        val path = clazz.getName.replace('.', '/') + "." + view + postfix
        val fullPath = if (prefix.isEmpty) {"/" + path} else {"/" + prefix + "/" + path}
        if (engine.resourceLoader.exists(fullPath)) {
          return fullPath
        }
      }
      null
    }

    def searchForView(): String = {
      for (i <- classSearchList) {
        val rc = viewForClass(i)
        if (rc != null) {
          return rc;
        }
      }
      null
    }

    buildClassList(model.getClass)
    val templateUri = searchForView()

    if (templateUri == null) {
      model.toString
    } else {
      using(model) {
        include(templateUri)
      }
    }
  }

  def renderTemplate(path: String, attrMap: Map[String, Any] = Map())(body: () => String): String = {
    capture {
      // TODO should we call engine.layout() instead??

      val uri = resolveUri(path)
      val bodyText = body()
      val context = this
      withAttributes(attrMap + ("body" -> bodyText)) {
        withUri(uri) {
          engine.load(uri).render(context);
        }
      }
    }
  }

  /**
   * uses the new sets of attributes for the given block, then replace them all
   * (and remove any newly defined attributes)
   */
  def withAttributes(attrMap: Map[String, Any])(block: => Unit): Unit = {
    val oldValues = new HashMap[String, Any]

    // lets replace attributes, saving the old vlaues
    for ((key, value) <- attrMap) {
      val oldValue = attributes.get(key)
      if (oldValue.isDefined) {
        oldValues.put(key, oldValue.get)
      }
      attributes(key) = value
    }

    block

    // restore old values
    for (key <- attrMap.keysIterator) {
      val oldValue = oldValues.get(key)
      setAttribute(key, oldValue)
    }
  }


  protected def withUri(uri: String)(block: => Unit): Unit = {
    val original = currentTemplate
    try {
      currentTemplate = uri
      block
    } finally {
      currentTemplate = original
    }
  }


  protected def resolveUri(path: String) = if (currentTemplate != null) {
    engine.resourceLoader.resolve(currentTemplate, path);
  } else {
    path
  }


  def setAttribute(name: String, value: Option[Any]): Unit = {
    if (value.isDefined) {
      attributes(name) = value.get
    }
    else {
      attributes.remove(name)
    }
  }

  protected def using[T](model: AnyRef)(op: => T): T = {
    val original = attributes.get("it");
    try {
      attributes("it") = model
      op
    } finally {
      setAttribute("it", original)
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

  /**
   * Evaluates the template capturing any output written to this page context during the body evaluation
   */
  def capture(template: Template): String = {
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      template.render(this)
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }


  /////////////////////////////////////////////////////////////////////
  //
  // resource helpers/accessors
  //
  /////////////////////////////////////////////////////////////////////

  private val resourceBeanAttribute = "it"

  /**
   * Returns the JAXRS resource bean of the given type or a                  { @link NoValueSetException } exception is thrown
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
