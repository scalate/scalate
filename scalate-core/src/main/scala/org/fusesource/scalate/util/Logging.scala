package org.fusesource.scalate.util

import org.slf4j.LoggerFactory

/**
 * @version $Revision : 1.1 $
 */

trait Logging {
  val log = LoggerFactory.getLogger(getClass.getName)

  def error(fn: => String): Unit = log.error(fn)
  def error(e: Throwable): Unit = error(e.getMessage, e)
  def error(message: String, e: Throwable): Unit = log.error(message, e)

  def warn(fn: => String): Unit = log.warn(fn)

  def info(fn: => String): Unit = log.info(fn)

  def debug(fn: => String): Unit = log.debug(fn)

  def trace(fn: => String): Unit = log.trace(fn)

}