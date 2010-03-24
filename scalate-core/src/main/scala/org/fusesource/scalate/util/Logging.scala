package org.fusesource.scalate.util

import org.slf4j.LoggerFactory

/**
 * @version $Revision : 1.1 $
 */

trait Logging {
  val log = LoggerFactory.getLogger(getClass.getName)

  def severe(fn: => String): Unit = log.error(fn)

  def warning(fn: => String): Unit = log.warn(fn)

  def info(fn: => String): Unit = log.info(fn)

  def debug(fn: => String): Unit = log.debug(fn)
  
  def fine(fn: => String): Unit = log.debug(fn)

  def finer(fn: => String): Unit = log.trace(fn)

}