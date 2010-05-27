package org.fusesource.scalate.servlet

import org.fusesource.scalate.TemplateEngine

/**
 * Some helper methods for servlet and web app based code
 */
object ServletHelper {

  /**
   * The default URIs to look for error templates
   */
  def errorUris(errorCode: String = "500"): List[String] = TemplateEngine.templateTypes.map("/WEB-INF/scalate/errors/" + errorCode + "." + _)


  /**
   * The default directories to look for templates
   */
  def templateDirectories = List("/WEB-INF", "")

}