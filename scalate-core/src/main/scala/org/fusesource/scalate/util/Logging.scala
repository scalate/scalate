package org.fusesource.scalate.util

import _root_.org.slf4j.{Logger, LoggerFactory}

/**
 * @version $Revision : 1.1 $
 */
object Logging {
  def apply(name: String): Logging = new LoggingImpl(LoggerFactory.getLogger(name))

  def apply(clazz: Class[_], postfix: String): Logging = apply(clazz.getName + "." + postfix)
}

trait Logging {
  def log: Logger = LoggerFactory.getLogger(getClass.getName)

  def error(fn: => String): Unit = log.error(fn)
  def error(e: Throwable): Unit = error(e.getMessage, e)
  def error(message: => String, e: Throwable): Unit = log.error(message, e)

  def warn(fn: => String): Unit = log.warn(fn)
  def warn(fn: => String, e: Throwable): Unit = log.warn(fn, e)

  def info(fn: => String): Unit = log.info(fn)
  def info(fn: => String, e: Throwable): Unit = log.info(fn, e)

  def debug(fn: => String): Unit = log.debug(fn)
  def debug(fn: => String, e: Throwable): Unit = log.debug(fn, e)

  def trace(fn: => String): Unit = log.trace(fn)
  def trace(fn: => String, e: Throwable): Unit = log.trace(fn, e)
}

class LoggingImpl(logger: Logger) extends Logging {
  override def log = logger
}