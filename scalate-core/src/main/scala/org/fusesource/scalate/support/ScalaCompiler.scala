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

    val classPathFromClassLoader = (new ClassPathBuilder).addPathFromContextClassLoader()
            .addPathFrom(classOf[Product])
            .addPathFrom(classOf[Global])
            .addPathFrom(getClass)
            .addPathFromSystemClassLoader()
            .addEntry(classpath)
            .addJavaPath()
            .classPath

    var useCP = if (classpath != null && combineClasspath) {
      classpath + pathSeparator + classPathFromClassLoader
    } else {
      classPathFromClassLoader
    }

    debug("using classpath: " + useCP)
    debug("system class loader: " + ClassLoader.getSystemClassLoader)
    debug("context class loader: " + Thread.currentThread.getContextClassLoader)
    debug("scalate class loader: " + getClass.getClassLoader)

    val settings = new Settings(errorHandler)
    settings.classpath.value = useCP
    settings.outdir.value = bytecodeDirectory.toString
    settings.deprecation.value = true
    //settings.unchecked.value = true

    // from play-scalate
    settings.debuginfo.value = "vars"
    settings.dependenciesFile.value = "none"
    settings.debug.value = false

    // TODO not sure if these changes make much difference?
    //settings.make.value = "transitivenocp"
    settings
  }
}
