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

package org.fusesource.scalate

import filter.FilterRequest
import introspector.Introspector
import support.RenderHelper
import util._
import util.Strings.isEmpty
import util.IOUtil._

import java.io.File
import java.text.{DateFormat, NumberFormat}
import java.util.{Locale, Date}
import collection.mutable.{ListBuffer, HashMap}
import xml.{Node, PCData, NodeSeq, NodeBuffer}

object RenderContext {
  val threadLocal = new ThreadLocal[RenderContext]

  def capture(body: => Unit) = apply().capture(body)

  def captureNodeSeq(body: => Unit) = apply().captureNodeSeq(body)

  def apply(): RenderContext = threadLocal.get

  @deprecated("Can leak thread local storage. Use the 'using' method instead.")
  def update(that: RenderContext) = threadLocal.set(that)
  
  def using[T](that: RenderContext)(func: =>T):T = {
    val previous = threadLocal.get
    try {
      threadLocal.set(that)
      func
    } finally {
      if( previous!=null ) {
        threadLocal.set(previous)
      } else {
        threadLocal.remove
      }
    }
  }

}

/**
 * Provides helper methods for rendering templates and values and for working with attributes.
 *
 * @see DefaultRenderContext
 * @see org.fusesource.scalate.servlet.ServletRenderContext
 */
trait RenderContext {
  /**
   * Default string used to output null values
   */
  var nullString = ""

  /**
   * Default string used to output None values
   */
  var noneString = ""

  /**
   * Whether or not markup sensitive characters for HTML/XML elements like &amp; &gt; &lt; are escaped or not
   */
  var escapeMarkup = true

  var currentTemplate: String = _

  var viewPrefixes = List("")
  var viewPostfixes = engine.codeGenerators.keysIterator.map(x => "." + x).toList

  def engine: TemplateEngine

  /**
   * Renders the provided value and inserts it into the final rendered document without sanitizing the value.
   */
  def <<(value: Any): Unit

  /**
   * Renders the provided value, sanitizes any XML special characters and inserts
   * it into the final rendered document.
   */
  def <<<(value: Any): Unit

  /**
   * Returns the request URI
   */
  def requestUri: String

  /**
   * Returns the Resource of the request
   */
  def requestResource: Option[Resource]

  /**
   * Returns the file for the given request resource
   */
  def requestFile: Option[File]


  /**
   * Returns a local link to the given file which should be within the [sourceDirectories]
   */
  def uri(file: File): Option[String] = {
    for (s <- engine.sourceDirectories) {
      if (Files.isDescendant(s, file)) {
        return Some(uri("/" + Files.relativeUri(s, file)))
      }
    }
    None
  }


  /**
   * Allows conversion of an absolute URL starting with "/" to be converted using the prefix of a web application
   */
  def uri(u: String): String = u

  /**
   * Access the attributes available in this context
   */
  def attributes: AttributeMap[String, Any]

  /**
   * Sorted list of attribute keys
   */
  def attributeKeys = attributes.keySet.toList.sortWith(_ < _)

  /**
   * Returns the attribute of the given type or a [[org.fussesource.scalate.NoValueSetException]] exception is thrown
   */
  def attribute[T](name: String): T =
    attributeOrElse(name, throw new NoValueSetException(name))


  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: => T): T = {
    attributes.get(name)
            .getOrElse(defaultValue)
            .asInstanceOf[T]
  }

  def setAttribute(name: String, value: Option[Any]) {
    value match {
      case Some(v) => attributes(name) = v
      case None => attributes.remove(name)
    }
  }

  /**
   * Sets the given attribute name to be the captured body of the template
   */
  def captureAttribute(name: String)(body: => Unit): Unit = {
    val v = capture(body)
    attributes(name) = v
  }

  /**
   * Creates an instance of the given given type using dependency injection to inject the necessary values into
   * the object
   */
  def inject[T](implicit manifest: Manifest[T]): T = {
    val clazz = manifest.erasure
    Objects.tryInstantiate(clazz, List(this)) match {
      case Some(t) => t.asInstanceOf[T]
      case _ => throw new NoInjectionException(clazz)
    }
  }
  /////////////////////////////////////////////////////////////////////
  //
  // Rendering API
  //
  //////////////////////////////////x///////////////////////////////////
  def value(any: Any, shouldSanitize: Boolean = escapeMarkup): Any = {
    def sanitize(text: String): Any = if (shouldSanitize) {Unescaped(RenderHelper.sanitize(text))} else {text}

    any match {
      case u: Unit => ""
      case null => sanitize(nullString)
      case None => sanitize(noneString)
      case Some(a) => value(a, shouldSanitize)
      case Unescaped(text) => text
      case f: Function0[_] => value(f(), shouldSanitize)
      case v: String => sanitize(v)
      case v: Date => sanitize(dateFormat.format(v))
      case n: Double if n.isNaN => "NaN"
      case n: Float if n.isNaN => "NaN"
      case v: Double => sanitize(numberFormat.format(v))
      case v: Float => sanitize(numberFormat.format(v))
      case v: Number => sanitize(numberFormat.format(v))
      case f: FilterRequest => {
        // NOTE assume a filter does the correct sanitizing
        var rc = filter(f.filter, f.content.toString)
        rc
      }
      // No need to sanitize nodes as they are already sanitized
      case s: NodeBuffer =>
        // No need to sanitize nodes as they are already sanitized
        (s.foldLeft(new StringBuilder) {
          (rc, x) => x match {
            case cd: PCData => rc.append(cd.data)
            case _ => rc.append(x)
          }
        }).toString

      case n: Node => n.toString

      case x: Traversable[Any] =>
        x.map(value(_, shouldSanitize)).mkString("")

      // TODO for any should we use the renderView?
      case v: Any => sanitize(v.toString)
    }
  }

  def valueEscaped(any: Any) = value(any, true)

  def valueUnescaped(any: Any) = value(any, false)

  /**
   * Ensures that the string value of the parameter is not markup escaped
   */
  def unescape(v: Any): Unit = this << value(v, false)

  /**
   * Ensures that the string value of the parameter is always markup escaped
   */
  def escape(v: Any): Unit = this << value(v, true)

  def filter(name: String, content: String): String = {
    val context = this
    engine.filter(name) match {
      case None => throw new NoSuchFilterException(name)
      case Some(f) => f.filter(context, content)
    }
  }

  def include(path: String): Unit = include(path, false)
  def include(path: String, layout: Boolean): Unit = include(path, layout, Nil)

  /**
   * Includes the given template path
   *
   * @param layout if true then applying the layout the included template
   */
  def include(path: String, layout: Boolean, extraBindings: List[Binding]): Unit = {
    val uri = resolveUri(path)

    withUri(uri) {
      val template = engine.load(uri, extraBindings)
      if (layout) {
        engine.layout(template, this);
      }
      else {
        template.render(this);
      }
    }
  }

  protected def blankString: String = ""

  /**
   * Renders a collection of model objects with an optional separator
   */
  def collection(objects: Traversable[AnyRef], viewName: String = "index", separator: => Any = blankString): Unit = {
    var first = true
    for (model <- objects) {
      if (first) {
        first = false
      }
      else {
        this << separator
      }
      view(model, viewName)
    }
  }

  /**
   * Renders the view of the given model object, looking for the view in
   * packageName/className.viewName.ext
   */
  def view(model: AnyRef, viewName: String = "index"): Unit = {
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
        val path = clazz.getName.replace('.', '/') + "." + viewName + postfix
        val fullPath = if (isEmpty(prefix)) {"/" + path} else {"/" + prefix + "/" + path}
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
      throw new NoSuchViewException(model, viewName)
    } else {
      using(model) {
        include(templateUri)
      }
    }
  }

  /**
   * Allows a symbol to be used with arguments to the   { @link render } or  { @link layout } method such as
   * <code>render("foo.ssp", 'foo -> 123, 'bar -> 456)   {...}
   */
  implicit def toStringPair(entry: (Symbol, Any)): (String, Any) = (entry._1.name, entry._2)

  /**
   * Renders the given template with optional attributes
   */
  def render(path: String, attributeMap: Map[String, Any] = Map()): Unit = {
    // TODO should we call engine.layout() instead??

    val uri = resolveUri(path)
    val context = this

    withAttributes(attributeMap) {
      withUri(uri) {
        engine.load(uri).render(context);
      }
    }
  }

  /**
   * Renders the given template with optional attributes passing the body block as the *body* attribute
   * so that it can be layered out using the template.
   */
  def layout(path: String, attrMap: Map[String, Any] = Map())(body: => Unit): Unit = {
    val bodyText = capture(body)
    render(path, attrMap + ("body" -> bodyText))
  }

  /**
   * Uses the new sets of attributes for the given block, then replace them all
   * (and remove any newly defined attributes)
   */
  def withAttributes[T](attrMap: Map[String, Any])(block: => T): T = {
    val oldValues = new HashMap[String, Any]

    // lets replace attributes, saving the old values
    for ((key, value) <- attrMap) {
      val oldValue = attributes.get(key)
      if (oldValue.isDefined) {
        oldValues.put(key, oldValue.get)
      }
      attributes(key) = value
    }

    val answer = block

    // restore old values
    for (key <- attrMap.keysIterator) {
      val oldValue = oldValues.get(key)
      if (removeOldAttributes || oldValue.isDefined) {
        setAttribute(key, oldValue)
      }
    }

    answer
  }

  /**
   * Should we remove attributes from the context after we've rendered a child request?
   */
  protected def removeOldAttributes = true

  def withUri[T](uri: String)(block: => T): T = {
    val original = currentTemplate
    try {
      currentTemplate = uri

      // lets keep track of the templates
      attributes("scalateTemplates") = uri :: attributeOrElse[List[String]]("scalateTemplates", List()).distinct

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


  protected def using[T](model: AnyRef)(op: => T): T = {
    val original = attributes.get("it");
    try {
      attributes("it") = model
      op
    } finally {
      setAttribute("it", original)
    }
  }


  /**
   * Evaluates the specified body capturing any output written to this context
   * during the evaluation
   */
  def capture(body: => Unit): String

  /**
   * Evaluates the template capturing any output written to this page context during the body evaluation
   */
  def capture(template: Template): String

  /**
   * Captures the text of the body and then parses it as markup
   */
  def captureNodeSeq(body: => Unit): NodeSeq = XmlHelper.textToNodeSeq(capture(body))

  /**
   * Captures the text of the template rendering and then parses it as markup
   */
  def captureNodeSeq(template: Template): NodeSeq = XmlHelper.textToNodeSeq(capture(template))

  /*
    Note due to the implicit conversions being applied to => Unit only taking the last
    statement of the block as per this discussion:
    http://old.nabble.com/-scala--is-this-a-compiler-bug-or-just-a-surprising-language-quirk-%28or-newbie--lack-of-understanding-%3A%29-ts27917276.html

    then we can no longer support this approach which is a shame.

    So tags must take => Unit as a parameter - then either take Rendercontext as the first parameter block
    or use the RenderContext() to get the current active context for capturing.

    implicit def bodyToStringFunction(body: => Unit): () => String = {
      () => {
        println("capturing the body....")
        val answer = capture(body)
        println("captured body: " + answer)
        answer
      }
    }

    implicit def toBody(body: => Unit): Body = new Body(this, body)
  */


  /////////////////////////////////////////////////////////////////////
  //
  // introspection for dynamic templates or for archetype templates
  //
  /////////////////////////////////////////////////////////////////////
  def introspect(aType: Class[_]) = Introspector(aType)



  /////////////////////////////////////////////////////////////////////
  //
  // resource helpers/accessors
  //
  /////////////////////////////////////////////////////////////////////

  private var resourceBeanAttribute = "it"

  /**
   * Returns the JAXRS resource bean of the given type or a  [[org.fusesource.scalate.NoValueSetException]]
   * exception is thrown
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
  // shame we can't use 'lazy var' for this cruft...
  def numberFormat: NumberFormat = _numberFormat()

  def numberFormat_=(value: NumberFormat): Unit = _numberFormat(value)

  def percentFormat: NumberFormat = _percentFormat()

  def percentFormat_=(value: NumberFormat): Unit = _percentFormat(value)

  def dateFormat: DateFormat = _dateFormat()

  def dateFormat_=(value: DateFormat): Unit = _dateFormat(value)


  def locale: Locale = Locale.getDefault


  /**
   * Used to represent some text which does not need escaping
   */
  case class Unescaped(text: String) {
    override def toString = text
  }
}
