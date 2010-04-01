package org.fusesource.scalate.util

import _root_.java.lang.{Throwable, String}
import _root_.org.slf4j.{Logger, LoggerFactory}

/**
 * A helper class for logging up a Log
 */
object Logging {
  def apply(name: String): Log = new Log(LoggerFactory.getLogger(name))

  def apply(clazz: Class[_], postfix: String): Log = apply(clazz.getName + "." + postfix)
}

/**
 * A Logging trait you can mix into an implementation class without affecting its public API
 */
trait Logging {
  protected def log: Logger = LoggerFactory.getLogger(getClass.getName)

  protected def error(fn: => String): Unit = log.error(fn)

  protected def error(e: Throwable): Unit = log.error(e.getMessage, e)

  protected def error(message: => String, e: Throwable): Unit = log.error(message, e)

  protected def warn(fn: => String): Unit = log.warn(fn)

  protected def warn(fn: => String, e: Throwable): Unit = log.warn(fn, e)

  protected def info(fn: => String): Unit = log.info(fn)

  protected def info(fn: => String, e: Throwable): Unit = log.info(fn, e)

  protected def debug(fn: => String): Unit = log.debug(fn)

  protected def debug(fn: => String, e: Throwable): Unit = log.debug(fn, e)

  protected def trace(fn: => String): Unit = log.trace(fn)

  protected def trace(fn: => String, e: Throwable): Unit = log.trace(fn, e)
}

/**
 * A handy logger class which can be used for logging using a public API
 * which is handy if you want to create a number of Log objects in an implementation
 * class and delegate to them for logging .
 */
class Log(log: Logger) {

   def error(fn: => String): Unit = log.error(fn)

   def error(e: Throwable): Unit = log.error(e.getMessage, e)

   def error(message: => String, e: Throwable): Unit = log.error(message, e)

   def warn(fn: => String): Unit = log.warn(fn)

   def warn(fn: => String, e: Throwable): Unit = log.warn(fn, e)

   def info(fn: => String): Unit = log.info(fn)

   def info(fn: => String, e: Throwable): Unit = log.info(fn, e)

   def debug(fn: => String): Unit = log.debug(fn)

   def debug(fn: => String, e: Throwable): Unit = log.debug(fn, e)

   def trace(fn: => String): Unit = log.trace(fn)

   def trace(fn: => String, e: Throwable): Unit = log.trace(fn, e)}