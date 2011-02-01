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
import org.slf4j.{MDC, LoggerFactory}


import java.util.concurrent.atomic.AtomicLong

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object Log {
  def apply(name:String):Log = new Log {
    override lazy val log = LoggerFactory.getLogger(name)
  }
  def apply(clazz:Class[_]):Log = apply(clazz.getName.replace("$", "#").stripSuffix("#"))
  def apply(clazz:Class[_], suffix:String):Log = apply(clazz.getName.replace("$", "#").stripSuffix("#")+"."+suffix)

  val exception_id_generator = new AtomicLong(System.currentTimeMillis)
  def next_exception_id = exception_id_generator.incrementAndGet.toHexString
}

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
trait Log {
  import Log._

  lazy val log = LoggerFactory.getLogger(getClass.getName.replace("$", "#").stripSuffix("#"))

  private def with_throwable(e:Throwable)(func: =>Unit) = {
    if( e!=null ) {
      val stack_ref = if( log.isDebugEnabled ) {
        val id = next_exception_id
        MDC.put("stackref", id.toString);
        Some(id)
      } else {
        None
      }
      func
      stack_ref.foreach { id=>
        log.debug("stack trace: "+id, e)
        MDC.remove("stackref")
      }
    } else {
      func
    }
  }

  private def format(message:String, args:Seq[Any]) = {
    if( args.isEmpty ) {
      message
    } else {
      message.format(args.map(_.asInstanceOf[AnyRef]) : _*)
    }
  }

  def error(m: => String, args:Any*): Unit = {
    if( log.isErrorEnabled ) {
      log.error(format(m, args.toSeq))
    }
  }

  def error(e: Throwable, m: => String, args:Any*): Unit = {
    if( log.isErrorEnabled ) {
      with_throwable(e) {
        log.error(format(m, args.toSeq))
      }
    }
  }

  def error(e: Throwable): Unit = {
    if( log.isErrorEnabled ) {
      with_throwable(e) {
        log.error(e.getMessage)
      }
    }
  }

  def warn(m: => String, args:Any*): Unit = {
    if( log.isWarnEnabled ) {
      log.warn(format(m, args.toSeq))
    }
  }

  def warn(e: Throwable, m: => String, args:Any*): Unit = {
    if( log.isWarnEnabled ) {
      with_throwable(e) {
        log.warn(format(m, args.toSeq))
      }
    }
  }

  def warn(e: Throwable): Unit = {
    if( log.isWarnEnabled ) {
      with_throwable(e) {
        log.warn(e.getMessage)
      }
    }
  }

  def info(m: => String, args:Any*): Unit = {
    if( log.isInfoEnabled ) {
      log.info(format(m, args.toSeq))
    }
  }

  def info(e: Throwable, m: => String, args:Any*): Unit = {
    if( log.isInfoEnabled ) {
      with_throwable(e) {
        log.info(format(m, args.toSeq))
      }
    }
  }

  def info(e: Throwable): Unit = {
    with_throwable(e) {
      if( log.isInfoEnabled ) {
        log.info(e.getMessage)
      }
    }
  }

  def debug(m: => String, args:Any*): Unit = {
    if( log.isDebugEnabled ) {
      log.debug(format(m, args.toSeq))
    }
  }

  def debug(e: Throwable, m: => String, args:Any*): Unit = {
    if( log.isDebugEnabled ) {
      log.debug(format(m, args.toSeq), e)
    }
  }

  def debug(e: Throwable): Unit = {
    if( log.isDebugEnabled ) {
      log.debug(e.getMessage, e)
    }
  }

  def trace(m: => String, args:Any*): Unit = {
    if( log.isTraceEnabled ) {
      log.trace(format(m, args.toSeq))
    }
  }

  def trace(e: Throwable, m: => String, args:Any*): Unit = {
    if( log.isTraceEnabled ) {
      log.trace(format(m, args.toSeq), e)
    }
  }

  def trace(e: Throwable): Unit = {
    if( log.isTraceEnabled ) {
      log.trace(e.getMessage, e)
    }
  }

}


/**
 * A Logging trait you can mix into an implementation class without affecting its public API
 */
trait Logging {

  protected val log = Log(getClass)

  protected def error(message: =>String): Unit = log.error(message)
  protected def error(message: =>String, e: Throwable): Unit = log.error(e, message)
  protected def error(e: Throwable): Unit = log.error(e)

  protected def warn(message: =>String): Unit = log.warn(message)
  protected def warn(message: =>String, e: Throwable): Unit = log.warn(e, message)
  protected def warn(e: Throwable): Unit = log.warn(e)

  protected def info(message: =>String): Unit = log.info(message)
  protected def info(message: =>String, e: Throwable): Unit = log.info(e, message)
  protected def info(e: Throwable): Unit = log.info(e)

  protected def debug(message: =>String): Unit = log.debug(message)
  protected def debug(message: =>String, e: Throwable): Unit = log.debug(e, message)
  protected def debug(e: Throwable): Unit = log.debug(e)

  protected def trace(message: =>String): Unit = log.trace(message)
  protected def trace(message: =>String, e: Throwable): Unit = log.trace(e, message)
  protected def trace(e: Throwable): Unit = log.trace(e)
}

