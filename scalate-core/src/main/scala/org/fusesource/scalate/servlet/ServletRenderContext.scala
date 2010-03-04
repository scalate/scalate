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
      if (value == null) {
        None
      } else {
        Some(value)
      }
    }

    def apply(key: String): Any = {
      if ("context" == key) {
        ServletRenderContext.this
      } else {
        request.getAttribute(key)
      }
    }

    def update(key: String, value: Any): Unit = {
      if (value == null) {
        request.removeAttribute(key)
      }
      else {
        request.setAttribute(key, value)
      }
    }

    def remove(key: String) = {
      val answer = get(key)
      request.removeAttribute(key)
      answer
    }
  }

  override def locale: Locale = {
    var locale = request.getLocale
    if (locale == null) {
      Locale.getDefault
    }
    else {
      locale
    }
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

}
