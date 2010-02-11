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

import filter.{MarkdownFilter, EscapedFilter, JavascriptFilter, PlainFilter}
import scaml.ScamlCodeGenerator
import java.net.URLClassLoader
import scala.collection.mutable.HashMap
import scala.compat.Platform
import ssp.{SspCodeGenerator, ScalaCompiler}
import util.IOUtil
import java.io.{File}

/**
 * A TemplateEngine is used to compile and load Scalate templates.
 * The TemplateEngine takes care of setting up the Scala compiler
 * and caching compiled templates for quicker subseqent loads
 * of a requested template.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateEngine {

  private case class CacheEntry(template: Template, dependencies: Set[String], timestamp: Long) {
    def isStale() = dependencies.exists {
      resourceLoader.lastModified(_) > timestamp
    }
  }

  /**
   * Set to false if you don't want the template engine to ever cache any of the compiled templates.
   */
  var allowCaching = true

  /**
   * Set to false if you don't want the template engine to see if a previously compiled template needs
   * to be reloaded due to it being updated.
   */
  var allowReload = true

  /**
   *
   */
  var resourceLoader: ResourceLoader = new FileResourceLoader
  var codeGenerators: Map[String, CodeGenerator] = Map("ssp" -> new SspCodeGenerator, "scaml" -> new ScamlCodeGenerator)
  var filters: Map[String, Filter] = Map()

  // Attempt to load all the built in filters.. Some may not load do to missing classpath
  // dependencies.
  attempt( filters += "plain" -> PlainFilter );
  attempt( filters += "javascript"-> JavascriptFilter );
  attempt( filters += "escaped"->EscapedFilter );
  attempt( filters += "markdown"->MarkdownFilter );

  private def attempt[T](op: => T): Unit = {
      try {
          op
      } catch {
          case e:Throwable=>{
          }
      }
  }

  lazy val compiler = new ScalaCompiler(bytecodeDirectory, classpath)

  def sourceDirectory = new File(workingDirectory, "src")
  def bytecodeDirectory = new File(workingDirectory, "classes")

  var classpath: String = null
  
  private var _workingDirectory: File = null

  def workingDirectory: File = {
    // Use a temp working directory if none is configured.
    if( _workingDirectory == null ) {
      _workingDirectory = new File(new File(System.getProperty("java.io.tmpdir")), "_scalate");
    }
    _workingDirectory
  }

  def workingDirectory_=(value:File) = {
    this._workingDirectory = value
  }


  var classLoader = this.getClass.getClassLoader

  var bindings = List[Binding]()

  private val templateCache = new HashMap[String, CacheEntry]

  /**
   * Compiles a template without placing it in the template cache. Useful for temporary
   * templates or dynamically created templates.
   */
  def compile(uri: String, extraBindings:List[Binding] = Nil):Template = {
    compile_and_load(uri, extraBindings, 0)._1
  }

  /**
   * Generates the Scala code for a template.  Useful for generating scala code that
   * will then be compiled into the application as part of a build process.
   */
  def generateScala(uri: String, extraBindings:List[Binding] = Nil) = {
    generator(uri).generate(this, uri, bindings ::: extraBindings)
  }

  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(uri: String, extraBindings:List[Binding]= Nil): Template = {
    templateCache.synchronized {

      // Determine whether to build/rebuild the template, load existing .class files from the file system,
      // or reuse an existing template that we've already loaded
      templateCache.get(uri) match {

        // Not in the cache..
        case None =>
          val className = generator(uri).className(uri)
          try {
            // Try to load a pre-compiled template from the classpath
            cache(uri, load_compiled_entry(className))
          } catch {
            case e:Throwable => {
              // It was not pre-compiled... compile and load it.
              cache(uri, compile_and_load_entry(uri, extraBindings))
            }
          }

        // It was in the cache..
        case Some(entry) =>
          // check for staleness
          if (allowReload && entry.isStale)
            // re-compile it
            cache(uri, compile_and_load_entry(uri, extraBindings))
          else
            // Cache entry is valid
            entry.template

      }
    }
  }

  private def load_compiled_entry(className:String) = {
    val cl = new URLClassLoader(Array(bytecodeDirectory.toURI.toURL), classLoader)
    val clazz = cl.loadClass(className)
    val template = clazz.asInstanceOf[Class[Template]].newInstance
    CacheEntry(template, Set(), Platform.currentTime)
  }

  private def compile_and_load_entry(uri:String, extraBindings:List[Binding]) = {
    val (template, dependencies) = compile_and_load(uri, extraBindings, 0)
    CacheEntry(template, dependencies, Platform.currentTime)
  }

  private def cache(uri:String, ce:CacheEntry) :Template = {
    if( allowCaching ) {
      templateCache += (uri -> ce)
    }
    ce.template
  }

  private def compile_and_load(uri: String, extraBindings:List[Binding], attempt:Int): (Template, Set[String]) = {
    try {

      // Generate the scala source code from the template
      val code = generateScala(uri, extraBindings)

      // Write the source code to file..
      val sourceFile = new File(sourceDirectory, uri+".scala")
      sourceFile.getParentFile.mkdirs
      IOUtil.writeBinaryFile(sourceFile, code.source.getBytes("UTF-8"))

      // Compile the generated scala code
      compiler.compile(sourceFile)

      // Load the compiled class and instantiate the template object
      val template = load_compiled_entry(code.className).template

      (template, code.dependencies)
    } catch {
      // TODO: figure out why we sometimes get these InstantiationException errors that
      // go away if you redo
      case e:InstantiationException=>{
        if( attempt ==0 ) {
          compile_and_load(uri, extraBindings, 1)
        } else {
          throw new TemplateException("Could not load template: "+e, e);
        }
      }
      case e:Throwable=>{
        throw new TemplateException("Could not load template: "+e, e);
      }
    }
  }

  /**
   * Gets the code generator to use for the give uri string by looking up the uri's extension
   * in the the codeGenerators map.
   */
  private def generator(uri: String): CodeGenerator = {
    val t = uri.split("\\.")
    if (t.length < 2) {
      throw new TemplateException("Template file extension missing.  Cannot determine which template processor to use.");
    } else {
      val extension = t.last
      codeGenerators.get(extension) match {
        case None => throw new TemplateException("Not a template file extension (" + codeGenerators.keysIterator.mkString("|") + "), you requested: " + extension);
        case Some(generator) => generator
      }
    }
  }

}

