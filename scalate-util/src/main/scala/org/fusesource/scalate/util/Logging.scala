/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.scalate.util

import _root_.java.lang.{Throwable, String}
import _root_.org.slf4j.{Logger, LoggerFactory}
import java.util.concurrent.atomic.AtomicLong

/**
 * A helper class for logging up a Log
 */
object Log {

  def apply(name: String): Log = new Log(LoggerFactory.getLogger(name))
  def apply(clazz: Class[_], postfix: String): Log = {
    val className = clazz.getName.stripSuffix("$")
    // Logback doesn't like "." to come after "$"
    // See http://jira.qos.ch/browse/LBCLASSIC-102
    val delimiter = if (className.contains("$")) "$" else "."
    apply(className + delimiter + postfix)
  }
  def apply(clazz: Class[_]): Log = apply(clazz.getName.stripSuffix("$"))

  val exception_id_generator = new AtomicLong(System.currentTimeMillis)
  def next_exception_id = exception_id_generator.incrementAndGet.toHexString

}

/**
 * A handy logger class which can be used for logging using a public API
 * which is handy if you want to create a number of Log objects in an implementation
 * class and delegate to them for logging .
 */
class Log(val log: Logger) {

  import Log._

  /**
   * Logs the trace trace with /w a new stack trace id at debug level
   * and returns a string that correlates to the stack trace id
   */
  private def stack(e: Throwable) = if (log.isDebugEnabled) {
    val id = next_exception_id
    log.debug("(stack:"+id+")", e)
    " (stack:"+id+")"
  } else {
    ""
  }

  def error(message: => String): Unit = if (log.isErrorEnabled) log.error(message)
  def error(message: => String, e: Throwable): Unit = error(message+stack(e))
  def error(e: Throwable): Unit = error(e.getMessage, e)

  def warn(message: => String): Unit = if (log.isWarnEnabled) log.warn(message)
  def warn(message: => String, e: Throwable): Unit = warn(message+stack(e))
  def warn(e: Throwable): Unit = warn(e.getMessage, e)

  def info(message: => String): Unit = if (log.isInfoEnabled) log.info(message)
  def info(message: => String, e: Throwable): Unit = info(message+stack(e))
  def info(e: Throwable): Unit = info(e.getMessage, e)

  def debug(message: => String): Unit = if (log.isDebugEnabled()) log.debug(message)
  def debug(message: => String, e: Throwable): Unit = if (log.isDebugEnabled()) log.debug(message, e)
  def debug(e: Throwable): Unit = debug(e.getMessage, e)

  def trace(message: => String): Unit = if (log.isTraceEnabled()) log.trace(message)
  def trace(message: => String, e: Throwable): Unit = if (log.isTraceEnabled()) log.trace(message, e)
  def trace(e: Throwable): Unit = trace(e.getMessage, e)

}

/**
 * A Logging trait you can mix into an implementation class without affecting its public API
 */
trait Logging {

  private lazy val _log = Log(getClass)
  protected def log:Log = _log

  protected def error(message: =>String): Unit = log.error(message)
  protected def error(message: =>String, e: Throwable): Unit = log.error(message, e)
  protected def error(e: Throwable): Unit = log.error(e)

  protected def warn(message: =>String): Unit = log.warn(message)
  protected def warn(message: =>String, e: Throwable): Unit = log.warn(message, e)
  protected def warn(e: Throwable): Unit = log.warn(e)

  protected def info(message: =>String): Unit = log.info(message)
  protected def info(message: =>String, e: Throwable): Unit = log.info(message, e)
  protected def info(e: Throwable): Unit = log.info(e)

  protected def debug(message: =>String): Unit = log.debug(message)
  protected def debug(message: =>String, e: Throwable): Unit = log.debug(message, e)
  protected def debug(e: Throwable): Unit = log.debug(e)

  protected def trace(message: =>String): Unit = log.trace(message)
  protected def trace(message: =>String, e: Throwable): Unit = log.trace(message, e)
  protected def trace(e: Throwable): Unit = log.trace(e)
}


