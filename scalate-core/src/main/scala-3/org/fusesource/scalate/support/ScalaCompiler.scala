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
import org.fusesource.scalate.*
import org.fusesource.scalate.util.{ ClassPathBuilder, Log }

import scala.runtime.ByteRef
import scala.util.parsing.input.OffsetPosition
import dotty.tools.*
import dotty.tools.dotc.reporting.{ ConsoleReporter, Reporter }
import org.fusesource.scalate.support.Compiler

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
  def compile(file: File): Unit = {
    synchronized {

      val reporter = dotty.tools.dotc.Main.process((settings :+ file.toString).toArray)

      if (reporter.hasErrors) {
        println(reporter.summary)
        val errors = reporter.allErrors.map(e => CompilerError(e.pos.source.file.path, e.message, OffsetPosition(String(e.pos.source.content()), e.pos.start)))
        throw new CompilerException("Compilation failed:\n" + reporter.summary, errors)
      }
    }
  }

  override def shutdown(): Unit = () // = compiler.askShutdown()
  protected def generateSettings(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean): Seq[String] = {
    bytecodeDirectory.mkdirs

    val pathSeparator = File.pathSeparator

    val classPathFromClassLoader = (new ClassPathBuilder)
      .addEntry(classpath)
      .addPathFromContextClassLoader()
      .addPathFrom(classOf[Product])
      .addPathFrom(classOf[Settings])
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

    val args = Seq(
      "-classpath",
      useCP,
      "-d",
      bytecodeDirectory.toString)

    args
  }
}
