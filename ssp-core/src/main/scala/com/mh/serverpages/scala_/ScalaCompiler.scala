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


package com.mh.serverpages.scala_

import com.mh.serverpages._
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.ServletConfig
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import java.net.URLClassLoader
import javax.servlet.http.HttpServlet


class ScalaCompiler extends Compiler
{
  override def compile(code: String, sourceDirectory: File, bytecodeDirectory: File, servletConfig: ServletConfig): Unit = {
    // Prepare an object for collecting error messages from the compiler
    val messageCollector = new StringWriter
    val messageCollectorWrapper = new PrintWriter(messageCollector)

    // Initialize the compiler
    val settings = generateSettings(bytecodeDirectory, buildClassPath(servletConfig))
    val reporter = new ConsoleReporter(settings, Console.in, messageCollectorWrapper)
    val compiler = new Global(settings, reporter)

    // Attempt compilation
    (new compiler.Run).compile(findFiles(sourceDirectory).map(_.toString))

    // Bail out if compilation failed
    if (reporter.hasErrors) {
      reporter.printSummary
      messageCollectorWrapper.close
      throw new ServerPageException("Compilation failed:\n" + messageCollector.toString)
    }
  }


  private def error(message: String): Unit = throw new ServerPageException("Compilation failed:\n" + message)


  private def buildClassPath(servletConfig: ServletConfig): String = {
    val containerList = classLoaderList(getClass) ::: classLoaderList(classOf[HttpServlet])

    // Always include WEB-INF/classes and all the JARs in WEB-INF/lib just in case
    val classesDirectory = servletConfig.getServletContext.getRealPath("/WEB-INF/classes")
    val libDirectory = servletConfig.getServletContext.getRealPath("/WEB-INF/lib")
    val jars = findFiles(new File(libDirectory)).map {_.toString}

    // Allow adding a classpath prefix & suffix via web.xml
    val prefix = servletConfig.getInitParameter("compiler.classpath.prefix") match {
      case null => Nil
      case path: String => List(path)
    }
    val suffix = servletConfig.getInitParameter("compiler.classpath.suffix") match {
      case null => Nil
      case path: String => List(path)
    }

    // Put the pieces together

    // TODO we should probably be smart enough to filter out duplicates here...
    (prefix ::: containerList ::: classesDirectory :: jars ::: suffix ::: Nil).mkString(":")
  }

  private def classLoaderList[T](aClass: Class[T]): List[String] = {
    aClass.getClassLoader match {
      case u: URLClassLoader =>
        List.fromArray(u.getURLs).map {_.getFile}

      case _ => Nil
    }
  }

  private def generateSettings(bytecodeDirectory: File, classpath: String): Settings = {
    println("using classpath: " + classpath)

    val settings = new Settings(error)
    settings.classpath.value = classpath
    settings.outdir.value = bytecodeDirectory.toString
    settings.deprecation.value = true
    settings.unchecked.value = true
    settings
  }


  private def findFiles(root: File): List[File] = {
    if (root.isFile)
      List(root)
    else
      makeList(root.listFiles).flatMap {f => findFiles(f)}
  }


  private def makeList(a: Array[File]): List[File] = {
    if (a == null)
      Nil
    else
      a.toList
  }

}
