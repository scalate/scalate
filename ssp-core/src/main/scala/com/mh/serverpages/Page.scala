package com.mh.serverpages

import javax.servlet.ServletException
import javax.servlet.http._
import java.io.PrintWriter

class NoSuchAttributeException(val attribute:String) extends ServletException("No such attribute '" + attribute + "'") {
}

/**
 * The PageContext provides helper methods for interacting with the request, response, attributes and parameters
 */
case class PageContext(out: PrintWriter, request: HttpServletRequest, response: HttpServletResponse) {

  /**
   * Returns the attribute of the given type or a {@link NoSuchAttributeException} exception is thrown
   */
  def attribute[T](name: String): T = {
    val value = request.getAttribute(name)
    if (value != null) {
      value.asInstanceOf[T]
    }
    else {
      throw new NoSuchElementException(name)
    }
  }

  /**
   * Returns the attribute of the given name and type or the default value specified
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
}

/**
 * Defines a bunch of helper methods available to SSP pages 
 *
 * @version $Revision: 1.1 $
 */
class Page extends HttpServlet {

  def createPageContext(out: PrintWriter, request: HttpServletRequest, response: HttpServletResponse) = {
    PageContext(out, request, response)
  }
}