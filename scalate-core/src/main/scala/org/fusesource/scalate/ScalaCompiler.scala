/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package org.fusesource.scalate.ssp

import org.fusesource.scalate._
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import org.fusesource.scalate.util.ClassLoaders._
import org.fusesource.scalate.util.Logging


class ScalaCompiler(bytecodeDirectory: File, classpath: String) extends Logging {

  val settings = generateSettings(bytecodeDirectory, classpath)
  val compiler = new Global(settings, null)

  def compile(file:File): Unit = {
    
    synchronized {
      val messageCollector = new StringWriter
      val messageCollectorWrapper = new PrintWriter(messageCollector)
      val reporter = new ConsoleReporter(settings, Console.in, messageCollectorWrapper)
      compiler.reporter = reporter

      // Attempt compilation
      (new compiler.Run).compile(List(file.getCanonicalPath))

      // Bail out if compilation failed
      if (reporter.hasErrors) {
        reporter.printSummary
        messageCollectorWrapper.close
        throw new TemplateException("Compilation failed:\n" +messageCollector)
      }
    }
  }

  private def error(message: String): Unit = throw new TemplateException("Compilation failed:\n" + message)

  private def generateSettings(bytecodeDirectory: File, classpath: String): Settings = {
    bytecodeDirectory.mkdirs

    def useCP = if (classpath != null) {
      classpath
    } else {
      (classLoaderList(Thread.currentThread.getContextClassLoader) ::: classLoaderList(getClass) ::: classLoaderList(ClassLoader.getSystemClassLoader) ).mkString(File.pathSeparator)
    }

    fine("using classpath: " + useCP)

    val settings = new Settings(error)
    settings.classpath.value = useCP
    settings.outdir.value = bytecodeDirectory.toString
    settings.deprecation.value = true
    settings.unchecked.value = true
    settings
  }

}
