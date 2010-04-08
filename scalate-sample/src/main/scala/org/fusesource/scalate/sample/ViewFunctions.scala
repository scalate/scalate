package org.fusesource.scalate.sample

import _root_.javax.servlet.http.HttpServletRequest

/**
 * @version $Revision : 1.1 $
 */
object ViewFunctions {
  def encode(path: String)(implicit request: HttpServletRequest): String = {
    "encoded '" + path + "' using request: " + request
  }
}