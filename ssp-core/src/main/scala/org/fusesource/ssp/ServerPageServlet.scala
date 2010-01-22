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


package org.fusesource.ssp

import java.io.File
import java.net.URLClassLoader
import javax.servlet.ServletConfig
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.mutable.HashMap
import scala.compat.Platform


private class CompiledPage(val servlet: HttpServlet, val timestamp: Long, val dependencies: Set[String])


abstract class ServerPageServlet extends HttpServlet
{
  val pageFileEncoding = "UTF-8"
  val allowReload = true

  val translationUnitLoader: TranslationUnitLoader
  val codeGenerator: CodeGenerator
  val compiler: Compiler

  private var workingDirectoryRoot: File = null
  private val compiledPages = new HashMap[String, CompiledPage]


  override def init(config: ServletConfig) = {
    super.init(config)

    // Initialize the working directory, within which we'll write generated source code and .class files
    workingDirectoryRoot = new File(getServletContext.getRealPath("WEB-INF/_serverpages/"))
    workingDirectoryRoot.mkdirs
  }


  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    // Get our ducks in a row before we get started
    val timestamp = Platform.currentTime
    val uri = request.getServletPath
    val workingDirectory = new File(workingDirectoryRoot, uri)
    val bytecodeDirectory = new File(workingDirectory, "bytecode")

    // Obtain the servlet object that should service this request (creating it on the fly if needed)
    val servlet = compiledPages.synchronized {
      // Determine whether to build/rebuild the page's servlet, load existing .class files from the filesystem,
      // or reuse an existing servlet that we've already loaded
      val cachedPage = compiledPages.get(uri)
      val loadMethod = cachedPage match {
        case None =>
          val ch = listFiles(bytecodeDirectory)
          if (bytecodeDirectory.exists && ch.length > 0 && findNewestTimestamp(ch, bytecodeDirectory.lastModified) >= findURITimestamp(uri))
            'LoadPrebuilt
          else
            'Build
        case Some(page) =>
          if (allowReload && page.dependencies.exists {findURITimestamp(_) > page.timestamp})
            'Build
          else
            'AlreadyLoaded
      }

      // Build/rebuild the page's servlet or reuse the already built servlet, as appropriate
      val pageToUse = loadMethod match {
        case 'AlreadyLoaded =>
          cachedPage.get
        case 'Build =>
          val newPage = preparePage(uri, timestamp)
          compiledPages += (uri -> newPage)
          newPage
        case 'LoadPrebuilt =>
          val className = codeGenerator.buildClassName(uri)
          println("Compling class name: " + className)
          val servlet = instantiateServlet(className, bytecodeDirectory)
          val dependencies = Set.empty[String] + uri //TODO: omits resources that 'uri' includes
          val newPage = new CompiledPage(servlet, timestamp, dependencies)
          compiledPages += (uri -> newPage)
          newPage
      }

      pageToUse.servlet
    }

    // Invoke the page's servlet
    servlet.service(request, response)
  }

  private def listFiles(bytecodeDirectory: File) = bytecodeDirectory.listFiles match {
    case null => Array[File]()
    case x: Array[File] => x
  }

  private def findNewestTimestamp(path: File): Long = {
    val c = listFiles(path)
    findNewestTimestamp(c, path.lastModified)
  }

  private def findNewestTimestamp(children: Array[File], defaultValue: Long): Long = {
    if (children.length == 0)
      defaultValue
    else
      children.foldLeft(defaultValue)((newestSoFar, file) => {
        val newestInSubtree = findNewestTimestamp(file)
        if (newestSoFar > newestInSubtree) newestSoFar else newestInSubtree
      })
  }


  private def findURITimestamp(uri: String): Long =
    new File(getServletContext.getRealPath(uri)).lastModified


  private def preparePage(uri: String, timestamp: Long): CompiledPage = {
    // Load the translation unit
    val translationUnit = translationUnitLoader.loadTranslationUnit(uri, getServletContext)

    // Prepare the working directory tree
    val workingDirectory = new File(workingDirectoryRoot, uri)
    deleteSubtree(workingDirectory)
    workingDirectory.mkdirs
    val sourceDirectory = new File(workingDirectory, "source")
    val bytecodeDirectory = new File(workingDirectory, "bytecode")
    sourceDirectory.mkdirs
    bytecodeDirectory.mkdirs

    // Convert the translation unit into executable code
    val sourceCode = codeGenerator.generateCode(translationUnit.content, sourceDirectory, uri)

    // Compile the generated code
    compiler.compile(sourceCode, sourceDirectory, bytecodeDirectory, getServletConfig)

    // Load the compiled class and instantiate the servlet object
    val servlet = instantiateServlet(codeGenerator.buildClassName(uri), bytecodeDirectory)

    new CompiledPage(servlet, timestamp, translationUnit.dependencies)
  }


  private def deleteSubtree(f: File): Unit = {
    if (f.isDirectory) {
      val children = f.listFiles match {
        case null => Array[File]()
        case x: Array[File] => x
      }
      children.foreach(deleteSubtree _)
    }

    f.delete
  }


  private def instantiateServlet(className: String, bytecodeDirectory: File): HttpServlet = {
    // Load the compiled class
    val classLoader = new URLClassLoader(Array(bytecodeDirectory.toURI.toURL), this.getClass.getClassLoader)
    println("Attempting to load class: '" + className + "'")
    val clazz = classLoader.loadClass(className)

    // Instantiate the servlet object
    val servlet: HttpServlet = clazz.asInstanceOf[Class[HttpServlet]].newInstance
    servlet.init( getServletConfig )
    servlet
  }

}
