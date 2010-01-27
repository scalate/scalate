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
package org.fusesource.scalate


import java.io.File
import java.net.URLClassLoader
import scala.collection.mutable.HashMap
import scala.compat.Platform
import ssp.ScalaCompiler
import util.IOUtil


class TemplateEngine {

  private class CacheEntry(val template: Template, val timestamp: Long, val dependencies: Set[String])

  var pageFileEncoding = "UTF-8"
  var allowReload = true

  var resourceLoader: ResourceLoader = null
  var codeGenerator: CodeGenerator = null
  var compiler: Compiler = new ScalaCompiler
  
  var classpath: String = null
  var workingDirectoryRoot: File = null

  private val templateCache = new HashMap[(String,List[TemplateArg]), CacheEntry]


  def load(uri: String, args:TemplateArg*) = {

    val argsList = args.toList;
    val key = (uri, args.toList)
    val timestamp = Platform.currentTime

    // TODO: mangle the params and include in the workingDirectory path
    val workingDirectory = new File(workingDirectoryRoot, uri)
    val bytecodeDirectory = new File(workingDirectory, "bytecode")

    // Obtain the template object that should service this request (creating it on the fly if needed)
    templateCache.synchronized {
      // Determine whether to build/rebuild the template, load existing .class files from the file system,
      // or reuse an existing template that we've already loaded
      val cacheEntry = templateCache.get(key)
      ((cacheEntry match {
        case None =>
          val ch = listFiles(bytecodeDirectory)
          if (bytecodeDirectory.exists && ch.length > 0 && findNewestTimestamp(ch, bytecodeDirectory.lastModified) >= lastModified(uri))
            'LoadPrebuilt
          else
            'Build
        case Some(entry) =>
          if (allowReload && entry.dependencies.exists {lastModified(_) > entry.timestamp})
            'Build
          else
            'AlreadyLoaded
      }) match {
        case 'AlreadyLoaded =>
          cacheEntry.get
        case 'Build =>
          val newCacheEntry = preparePage(timestamp, uri, argsList)
          templateCache += (key -> newCacheEntry)
          newCacheEntry
        case 'LoadPrebuilt =>
          val className = codeGenerator.className(uri, argsList)
          val template = createTemplate(className, bytecodeDirectory)
          val dependencies = Set.empty[String] + uri
          val newCacheEntry = new CacheEntry(template, timestamp, dependencies)
          templateCache += (key -> newCacheEntry)
          newCacheEntry
      }).template
    }
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


  private def lastModified(uri: String): Long =
    resourceLoader.lastModified(uri)


  private def preparePage(timestamp: Long, uri: String, args:List[TemplateArg]): CacheEntry = {

    // Convert the translation unit into executable code
    val code = codeGenerator.generate(this, uri, args)

    // Prepare the working directory tree
    val workingDirectory = new File(workingDirectoryRoot, uri)
    deleteSubtree(workingDirectory)
    workingDirectory.mkdirs
    val sourceDirectory = new File(workingDirectory, "source")
    val bytecodeDirectory = new File(workingDirectory, "bytecode")
    sourceDirectory.mkdirs
    bytecodeDirectory.mkdirs

    // Dump the generated source code to the working directory
    IOUtil.writeBinaryFile(new File(sourceDirectory, code.className + ".scala").toString, code.source.getBytes("UTF-8"))

    // Compile the generated code
    compiler.compile(code.source, sourceDirectory, bytecodeDirectory, classpath)

    // Load the compiled class and instantiate the template object
    val template = createTemplate(code.className, bytecodeDirectory)

    new CacheEntry(template, timestamp, code.dependencies)
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


  private def createTemplate(className: String, bytecodeDirectory: File): Template = {
    // Load the compiled class
    val classLoader = new URLClassLoader(Array(bytecodeDirectory.toURI.toURL), this.getClass.getClassLoader)
    val clazz = classLoader.loadClass(className)
    clazz.asInstanceOf[Class[Template]].newInstance
  }

}

