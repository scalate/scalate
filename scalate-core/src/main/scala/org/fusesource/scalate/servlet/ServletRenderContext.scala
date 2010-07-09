package org.fusesource.scalate.servlet

import _root_.org.fusesource.scalate.util.URIs._
import javax.servlet.{ServletConfig, ServletContext, ServletException}
import javax.servlet.http._
import java.lang.String
import java.util.{Locale}
import scala.collection.JavaConversions._
import scala.collection.Set
import scala.collection.mutable.HashSet
import org.fusesource.scalate.{RenderContext, AttributeMap, DefaultRenderContext, TemplateEngine}

/**
 * Easy access to servlet request state.
 *
 * If you add the following code to your program
 * <code>import org.fusesource.scalate.servlet.ServletRequestContext._</code>
 * then you can access the current renderContext, request, response, servletContext
 */
object ServletRenderContext {
  /**
   * Returns the currently active render context in this thread
   * @throw IllegalArgumentException if there is no suitable render context available in this thread
   */
  def renderContext: ServletRenderContext = RenderContext() match {
    case s: ServletRenderContext => s
    case n => throw new IllegalArgumentException("This threads RenderContext is not a ServletRenderContext as it is: " + n)
  }

  def request: HttpServletRequest = renderContext.request

  def response: HttpServletResponse = renderContext.response

  def servletContext: ServletContext = renderContext.servletContext
}

/**
 * A template context for use in servlets
 *
 * @version $Revision : 1.1 $
 */
class ServletRenderContext(engine: TemplateEngine, val request: HttpServletRequest, val response: HttpServletResponse, val servletContext: ServletContext) extends DefaultRenderContext(engine, response.getWriter) {
  viewPrefixes = List("WEB-INF", "")

  override val attributes = new AttributeMap[String, Any] {
    request.setAttribute("context", ServletRenderContext.this)

    def get(key: String): Option[Any] = {
      val value = apply(key)
      if (value == null) None else Some(value)
    }

    def apply(key: String): Any = key match {
      case "context" => ServletRenderContext.this
      case _ => request.getAttribute(key)
    }

    def update(key: String, value: Any): Unit = value match {
      case null => request.removeAttribute(key)
      case _ => request.setAttribute(key, value)
    }

    def remove(key: String) = {
      val answer = get(key)
      request.removeAttribute(key)
      answer
    }

    def keySet: Set[String] = {
      val answer = new HashSet[String]()
      for (a <- asIterator(request.getAttributeNames)) {
        answer.add(a.toString)
      }
      answer
    }

    override def toString = keySet.map(k => "" + k + " -> " + apply(k)).mkString("{", ", ", "}")
  }

  /**
   * Named servletConfig for historical reasons; actually returns a Config, which presents  a unified view of either a
   * ServletConfig or a FilterConfig.
   *
   * @return a Config, if the servlet engine is a ServletTemplateEngine
   * @throws IllegalStateException if the servlet engine is not a ServletTemplateEngine
   */
  def servletConfig: Config = engine match {
    case servletEngine: ServletTemplateEngine => servletEngine.config
    case _ => throw new IllegalArgumentException("render context not created with ServletTemplateEngine so cannot provide a ServletConfig")
  }

  override def locale: Locale = {
    var locale = request.getLocale
    if (locale == null) Locale.getDefault else locale
  }

  /**
   * Forwards this request to the given page
   */
  def forward(page: String) {

    def requestDispatcher = {
      val dispatcher = request.getRequestDispatcher(page)
      if (dispatcher == null) {
        throw new ServletException("No dispatcher available for path: " + page)
      }
      dispatcher
    }

    requestDispatcher.forward(request, response)
  }

  /**
   * Creates a URI which if the uri starts with / then the link is prefixed with the web applications context
   */
  def uri(uri: String) = {
    if (uri.startsWith("/")) {
      request.getContextPath + uri
    }
    else {
      uri
    }
  }

  /**
   * Returns the current URI with new query arguments (separated with &)
   */
  def currentUriPlus(newQueryArgs: String) = {
    uriPlus(requestURI, queryString, newQueryArgs)
  }


  /**
   * Returns the current URI with query arguments (separated with &) removed
   */
  def currentUriMinus(newQueryArgs: String) = {
    uriMinus(requestURI, queryString, newQueryArgs)
  }

  /**
   * Returns all of the parameter values
   */
  def parameterValues(name: String): Array[String] = {
    val answer = request.getParameterValues(name)
    if (answer != null) {
      answer
    }
    else {
      Array[String]()
    }
  }

  /**
   * Returns the first parameter
   */
  def parameter(name: String) = {request.getParameter(name)}

  /**
   * Returns the forwarded request uri or the current request URI if its not forwarded
   */
  def requestURI: String = attributes.get("javax.servlet.forward.request_uri") match {
    case Some(value: String) => value
    case _ => request.getRequestURI
  }

  /**
   * Returns the forwarded query string or the current query string if its not forwarded
   */
  def queryString: String = attributes.get("javax.servlet.forward.query_string") match {
    case Some(value: String) => value
    case _ => request.getQueryString
  }

  /**
   * Returns the forwarded context path or the current context path if its not forwarded
   */
  def contextPath: String = attributes.get("javax.servlet.forward.context_path") match {
    case Some(value: String) => value
    case _ => request.getContextPath
  }

}
