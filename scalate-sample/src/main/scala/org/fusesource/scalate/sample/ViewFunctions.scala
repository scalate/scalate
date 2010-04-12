package org.fusesource.scalate.sample

import _root_.javax.servlet.http.HttpServletRequest

/**
 * @version $Revision : 1.1 $
 */
object ViewFunctions {
  def encode(path: String)(implicit request: HttpServletRequest): String = {
    "encoded '" + path + "' using request: " + request
  }

  def encode2(path: String): String = {
    // lets import the request/response objects
    import org.fusesource.scalate.servlet.ServletRenderContext._
    
    "encoded2 '" + path + "' using request: " + request
  }
}