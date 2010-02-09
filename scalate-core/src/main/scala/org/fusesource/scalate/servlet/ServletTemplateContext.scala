package org.fusesource.scalate.servlet

import scala.xml.Node
import javax.servlet.http._
import org.fusesource.scalate.util.{Lazy}
import java.text.{DateFormat, NumberFormat}
import java.io._
import javax.servlet.{ServletOutputStream, ServletContext, RequestDispatcher, ServletException}
import java.lang.String
import collection.mutable.{Stack, ListBuffer, HashMap}
import java.util.{Properties, Date, Locale}
import org.fusesource.scalate.{TemplateEngine, DefaultRenderContext, NoSuchViewException, NoValueSetException}

/**
 * A template context for use in servlets
 *
 * @version $Revision: 1.1 $
 */
class ServletTemplateContext(engine:TemplateEngine, val request: HttpServletRequest, val response: HttpServletResponse, val servletContext: ServletContext) extends DefaultRenderContext(engine, response.getWriter) {
  
  viewPrefixes = List("WEB-INF", "")

  /**
   * Returns the attribute of the given type or a   { @link NoValueSetException } exception is thrown
   */
  override def binding(name: String) = {
    if( "context" == name ) {
      Some(this)
    } else {
      val value = request.getAttribute(name)
      if ( value == null ) {
        None
      } else {
        Some(value)
      }
    }
  }

  override def binding(name:String, value:Option[Any]): Unit = {
    value match {
      case None    => request.removeAttribute(name)
      case Some(v) => request.setAttribute(name, v)
    }
  }

  override def attribute[T](name: String): T = {
    val value = request.getAttribute(name)
    if (value != null) {
      value.asInstanceOf[T]
    }
    else {
      throw new NoValueSetException(name)
    }
  }

  /**
   * Returns the attribute of the given name and type or the default value if it is not available
   */
  override def attributeOrElse[T](name: String, defaultValue: T): T = {
    val value = request.getAttribute(name)
    if (value != null) {
      value.asInstanceOf[T]
    }
    else {
      defaultValue
    }
  }

  /**
   * Updates the named attribute with the given value
   */
  override def setAttribute[T](name: String, value: T): Unit = {
    request.setAttribute(name, value)
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
