package org.fusesource.scalate.servlet

import javax.servlet.http._
import javax.servlet.{ServletContext, ServletException}
import java.lang.String
import collection.mutable.{ListBuffer, HashMap}
import java.util.{Locale}
import org.fusesource.scalate.{AttributeMap, TemplateEngine, DefaultRenderContext}

/**
 * A template context for use in servlets
 *
 * @version $Revision : 1.1 $
 */
class ServletRenderContext(engine: TemplateEngine, val request: HttpServletRequest, val response: HttpServletResponse, val servletContext: ServletContext) extends DefaultRenderContext(engine, response.getWriter) {
  viewPrefixes = List("WEB-INF", "")

  override val attributes = new AttributeMap[String, Any] {
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
}
