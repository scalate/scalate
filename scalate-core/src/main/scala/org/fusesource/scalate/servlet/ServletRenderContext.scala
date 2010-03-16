package org.fusesource.scalate.servlet

import javax.servlet.http._
import javax.servlet.{ServletContext, ServletException}
import java.lang.String
import java.util.{Locale}
import scala.collection.JavaConversions._
import scala.collection.Set
import scala.collection.mutable.HashSet
import org.fusesource.scalate.{AttributeMap, TemplateEngine, DefaultRenderContext}

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
      case _         => request.getAttribute(key)
    }

    def update(key: String, value: Any): Unit = value match {
      case null => request.removeAttribute(key) 
      case _    => request.setAttribute(key, value)
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

    override def toString = keySet.map(k => k + " -> " + apply(k)).mkString("{", ", ", "}")
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

}
