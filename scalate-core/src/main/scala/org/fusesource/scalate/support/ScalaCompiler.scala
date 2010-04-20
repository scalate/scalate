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

package org.fusesource.scalate.support

import org.fusesource.scalate._
import util.ClassPathBuilder
import util.Logging
import util.Sequences.removeDuplicates
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import tools.nsc.util.{OffsetPosition, FakePos, NoPosition, Position}
import scala.util.parsing.input.OffsetPosition

class ScalaCompiler(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean = false) extends Logging {

  val settings = generateSettings(bytecodeDirectory, classpath, combineClasspath)
  val compiler = new Global(settings, null)

  def compile(file:File): Unit = {
    synchronized {
      val messageCollector = new StringWriter
      val messageCollectorWrapper = new PrintWriter(messageCollector)

      var messages = List[CompilerError]()
      val reporter = new ConsoleReporter(settings, Console.in, messageCollectorWrapper) {

        override def printMessage(posIn: Position, msg: String) {
          val pos = if (posIn eq null) NoPosition
                    else if (posIn.isDefined) posIn.inUltimateSource(posIn.source)
                    else posIn
          pos match {
            case FakePos(fmsg) =>
              super.printMessage(posIn, msg);
            case NoPosition =>
              super.printMessage(posIn, msg);
            case _ =>
              messages = CompilerError(posIn.source.file.file.getPath, msg, OffsetPosition(posIn.source.content, posIn.point)) :: messages
              super.printMessage(posIn, msg);
          }

        }
      }
      compiler.reporter = reporter

      // Attempt compilation
      (new compiler.Run).compile(List(file.getCanonicalPath))

      // Bail out if compilation failed
      if (reporter.hasErrors) {
        reporter.printSummary
        messageCollectorWrapper.close
        throw new CompilerException("Compilation failed:\n" +messageCollector, messages)
      }
    }
  }

  private def errorHandler(message: String): Unit = throw new TemplateException("Compilation failed:\n" + message)

  private def generateSettings(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean): Settings = {
    bytecodeDirectory.mkdirs

    val pathSeparator = File.pathSeparator

    var useCP = if (classpath != null && !combineClasspath) {
      classpath
    } else {
      (new ClassPathBuilder).addPathFromContextClassLoader()
                            .addPathFrom(classOf[Product])
                            .addPathFrom(classOf[Global])
                            .addPathFrom(getClass)
                            .addPathFromSystemClassLoader()
                            .addEntry(classpath)
                            .addJavaPath()
                            .classPath
    }

    debug("using classpath: " + useCP)

    val settings = new Settings(errorHandler)
    settings.classpath.value = useCP
    settings.outdir.value = bytecodeDirectory.toString
    settings.deprecation.value = true
    settings.unchecked.value = true
    // TODO not sure if these changes make much difference?
    //settings.make.value = "transitivenocp"
    settings
  }
}
