/**
 * Copyright (C) 2009-2011 the original author or authors.
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

import java.io.{ File, PrintWriter, StringWriter }

import org.fusesource.scalate._
import org.fusesource.scalate.util.{ ClassPathBuilder, Log }

import scala.reflect.internal.util.{ FakePos, NoPosition, Position }
import scala.runtime.ByteRef
import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.{ ConsoleReporter, Reporter }
import scala.util.parsing.input.OffsetPosition

object ScalaCompiler extends Log {

  def create(engine: TemplateEngine): ScalaCompiler = {
    Thread.currentThread.getContextClassLoader match {
      // TODO: https://github.com/scalate/scalate/pull/90
      // case BundleClassLoader(loader) => new OsgiScalaCompiler(engine, loader.getBundle)
      case _ => new ScalaCompiler(engine.bytecodeDirectory, engine.classpath, engine.combinedClassPath)
    }
  }

}

import org.fusesource.scalate.support.ScalaCompiler._

class ScalaCompiler(
  bytecodeDirectory: File,
  classpath: String,
  combineClasspath: Boolean = false) extends Compiler {

  val settings = generateSettings(bytecodeDirectory, classpath, combineClasspath)

  class LoggingReporter(writer: StringWriter = new StringWriter)
    extends ConsoleReporter(settings, Console.in, new PrintWriter(writer)) { self =>

    var compilerErrors: List[CompilerError] = Nil
    def messages = writer.toString

    override def reset(): Unit = {
      compilerErrors = Nil
      writer.getBuffer.setLength(0)
      writer.getBuffer.trimToSize()
      super.reset()
    }

    override def display(posIn: Position, msg: String, severity: Severity): Unit = {
      val pos = if (posIn eq null) NoPosition
      else if (posIn.isDefined) posIn.finalPosition
      else posIn
      pos match {
        case FakePos(_) => super.display(posIn, msg, severity)
        case NoPosition => super.display(posIn, msg, severity)
        case _ =>
          // Adding the detected compilation error
          compilerErrors ::= CompilerError(posIn.source.file.file.getPath, msg, OffsetPosition(posIn.source.content, posIn.point))

          super.display(posIn, msg, severity)
      }
    }

    def printSummary(): Unit = {
      import reflect.internal.util.StringOps.{ countElementsAsString => countAs }
      if (self.hasErrors) {
        display(NoPosition, s"""${countAs(self.errorCount, "error")} found""", ERROR)
      }
      if (self.hasWarnings) {
        display(NoPosition, s"""${countAs(self.warningCount, "warning")} found""", WARNING)
      }
    }

  }

  private[this] val reporter = new LoggingReporter

  val compiler = createCompiler(settings, reporter)

  def compile(file: File): Unit = {
    synchronized {
      reporter.reset()

      // Attempt compilation
      (new compiler.Run).compile(List(file.getCanonicalPath))

      // Bail out if compilation failed
      if (reporter.hasErrors) {
        reporter.printSummary()
        throw new CompilerException("Compilation failed:\n" + reporter.messages, reporter.compilerErrors)
      }
    }
  }

  override def shutdown(): Unit = () // = compiler.askShutdown()

  private def errorHandler(message: String): Unit = throw new TemplateException("Compilation failed:\n" + message)

  protected def generateSettings(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean): Settings = {
    bytecodeDirectory.mkdirs

    val pathSeparator = File.pathSeparator

    val classPathFromClassLoader = (new ClassPathBuilder)
      .addEntry(classpath)
      .addPathFromContextClassLoader()
      .addPathFrom(classOf[Product])
      .addPathFrom(classOf[Global])
      .addPathFrom(classOf[ByteRef])
      .addPathFrom(getClass)
      .addPathFromSystemClassLoader()
      .addJavaPath()
      .classPath

    val useCP: String = if (classpath != null && combineClasspath) {
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

    settings
  }

  protected def createCompiler(settings: Settings, reporter: Reporter): Global = {
    debug("creating non-OSGi compiler")
    new Global(settings, reporter)
  }
}