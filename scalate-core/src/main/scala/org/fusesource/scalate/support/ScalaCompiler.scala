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

import org.fusesource.scalate._
import osgi.{BundleHeaders, BundleClassPathBuilder, BundleClassLoader}
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.util._
import scala.util.parsing.input.OffsetPosition
import collection.mutable.ListBuffer
import org.osgi.framework.Bundle
import java.io.{PrintWriter, StringWriter, File}

import util.{Log, IOUtil, ClassPathBuilder}

object ScalaCompiler extends Log {

  def create(engine: TemplateEngine) : ScalaCompiler = {
    Thread.currentThread.getContextClassLoader match {
      case BundleClassLoader(loader) => new OsgiScalaCompiler(engine, loader.getBundle)
      case _ => new ScalaCompiler(engine.bytecodeDirectory, engine.classpath, engine.combinedClassPath)
    }
  }

}

import ScalaCompiler._

class ScalaCompiler(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean = false) {

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

  protected def generateSettings(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean): Settings = {
    bytecodeDirectory.mkdirs

    val pathSeparator = File.pathSeparator

    val classPathFromClassLoader = (new ClassPathBuilder)
            .addEntry(classpath)
            .addPathFromContextClassLoader()
            .addPathFrom(classOf[Product])
            .addPathFrom(classOf[Global])
            .addPathFrom(getClass)
            .addPathFromSystemClassLoader()
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

class OsgiScalaCompiler(val engine: TemplateEngine, val bundle: Bundle)
        extends ScalaCompiler(engine.bytecodeDirectory, engine.classpath, engine.combinedClassPath) {

  debug("Using OSGi-enabled Scala compiler")

  override val compiler = new Global(settings, null) {
    lazy val internalClassPath = {
      require(!forMSIL, "MSIL not supported")
      createClassPath(super.classPath)
    }

    override def rootLoader = new loaders.JavaPackageLoader(internalClassPath.asInstanceOf[ClassPath[AbstractFile]])

    def createClassPath[T](original: ClassPath[T]) = {
      var result = ListBuffer(original)
      val files = List.concat(
        BundleClassPathBuilder.fromBundle(bundle),
        List(BundleClassPathBuilder.create(BundleClassLoader.unapply(classOf[scala.Application].getClassLoader).get.getBundle),
             BundleClassPathBuilder.create(BundleClassLoader.unapply(classOf[ScalaCompiler].getClassLoader).get.getBundle),
             BundleClassPathBuilder.create(BundleClassLoader.unapply(classOf[ClassPathBuilder].getClassLoader).get.getBundle)))
      files.foreach(file => {
        debug("Adding bundle " + file + " to the Scala compiler classpath")
        result += original.context.newClassPath(file)
      })
      new MergedClassPath(result.toList.reverse, original.context)
    }
  }

  override protected def generateSettings(bytecodeDirectory: File, classpath: String, combineClasspath: Boolean): Settings = {
    engine.libraryDirectory.mkdirs

    // handle embedded bundle dependencies
    for (entry <- BundleHeaders(bundle).classPath; if entry.endsWith(".jar")) {
      val url = bundle.getResource(entry)
      val name = entry.split("/").last
      IOUtil.copy(url, new File(engine.libraryDirectory, name))
      debug("Extracting " + url.getFile + " into " + engine.libraryDirectory)
    }

    val builder = new ClassPathBuilder
    builder.addLibDir(engine.libraryDirectory.getAbsolutePath)

    super.generateSettings(bytecodeDirectory,
                           classpath + File.pathSeparator + builder.classPath,
                           combineClasspath)
  }
}

