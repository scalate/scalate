package org.fusesource.scalate.util

import java.util.logging.{Level, Logger}
import java.util.logging.Level._

/**
 * @version $Revision : 1.1 $
 */

trait Logging {
  val log = Logger.getLogger(this.getClass.getName)

  def severe(fn: => String): Unit = log(SEVERE, fn)

  def warning(fn: => String): Unit = log(WARNING, fn)

  def info(fn: => String): Unit = log(INFO, fn)

  def fine(fn: => String): Unit = log(FINE, fn)

  def finer(fn: => String): Unit = log(FINER, fn)

  def finest(fn: => String): Unit = log(FINEST, fn)

  def log(level: Level, fn: => String): Unit = {
    if (log.isLoggable(level)) {
      log.log(level, fn)
    }
  }
}