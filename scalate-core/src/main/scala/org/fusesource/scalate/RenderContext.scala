package org.fusesource.scalate

import filter.FilterRequest
import java.util.{Locale, Date}
import java.text.{DateFormat, NumberFormat}
import introspector.Introspector
import collection.mutable.{ListBuffer, HashMap}
import util.{RenderHelper, XmlHelper, Lazy}
import xml.{Node, PCData, NodeSeq, NodeBuffer}

object RenderContext {
  val threadLocal = new ThreadLocal[RenderContext]

  def capture(body: => Unit) = apply().capture(body)

  def captureNodeSeq(body: => Unit) = apply().captureNodeSeq(body)

  def apply(): RenderContext = threadLocal.get

  def update(that: RenderContext) = threadLocal.set(that)
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
   * Access the attributes available in this context
   */
  def attributes : AttributeMap[String,Any]


  /**
   * Sorted list of attribute keys
   */
  def attributeKeys = attributes.keySet.toList.sortWith(_<_)
  
  /**
   * Returns the attribute of the given type or a {@link NoValueSetException} exception is thrown
   */
  def attribute[T](name: String): T = {
    attributes.get(name)
              .getOrElse(throw new NoValueSetException(name))
              .asInstanceOf[T]
  }

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  def attributeOrElse[T](name: String, defaultValue: T): T = {
    attributes.get(name)
              .getOrElse(defaultValue)
              .asInstanceOf[T]
  }

  def setAttribute(name: String, value: Option[Any]) {
    value match {
      case Some(v) => attributes(name) = v
      case None    => attributes.remove(name)
    }
  }

  /////////////////////////////////////////////////////////////////////
  //
  // Rendering API
  //
  //////////////////////////////////x///////////////////////////////////
  def value(any: Any, shouldSanitize: Boolean = escapeMarkup): String = {
    def sanitize(text: String): String = if (shouldSanitize) {RenderHelper.sanitize(text)} else {text}

    any match {
      case u: Unit => ""
      case null => sanitize(nullString)
      case Unescaped(text) => text
      case v: String => sanitize(v)
      case v: Date => sanitize(dateFormat.format(v))
      case v: Number => sanitize(numberFormat.format(v))
      case f: FilterRequest => {
        // NOTE assume a filter does the correct sanitizing
        var rc = filter(f.filter, f.content)
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
        x.map( value(_, shouldSanitize) ).mkString("")


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
    engine.filters.get(name) match {
      case None => throw new NoSuchFilterException(name)
      case Some(f) => f.filter(content)
    }
  }

  def include(path: String): Unit = include(path, false)

  /**
   * Includes the given template path
   *
   * @param layout if true then applying the layout the included template
   */
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

  /**
   * Allows a symbol to be used with arguments to the  {@link render} or {@link layout} method such as
   * <code>render("foo.ssp", 'foo -> 123, 'bar -> 456)  {...}
   */
  implicit def toStringPair(entry: (Symbol,Any)): (String,Any) = (entry._1.name, entry._2)

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
  def withAttributes(attrMap: Map[String, Any])(block: => Unit): Unit = {
    val oldValues = new HashMap[String, Any]

    // lets replace attributes, saving the old values
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
      if (removeOldAttributes || oldValue.isDefined) {
        setAttribute(key, oldValue)
      }
    }
  }

  /**
   * Should we remove attributes from the context after we've rendered a child request?
   */
  protected def removeOldAttributes = true

  protected def withUri(uri: String)(block: => Unit): Unit = {
    val original = currentTemplate
    try {
      currentTemplate = uri

      // lets keep track of the templates
      attributes("scalateTemplates") = uri :: attributeOrElse[List[String]]("scalateTemplates", List()) 

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
   *  Returns the JAXRS resource bean of the given type or a  { @link NoValueSetException } exception is thrown
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
  case class Unescaped(text: String)
}
