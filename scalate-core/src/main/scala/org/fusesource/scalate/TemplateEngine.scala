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

import _root_.org.objectweb.asm.tree.ClassNode
import _root_.org.objectweb.asm.{ClassReader, ClassWriter}
import _root_.scala.util.parsing.input.{OffsetPosition, Position}
import filter.{MarkdownFilter, EscapedFilter, JavascriptFilter, PlainFilter}
import layout.DefaultLayoutStrategy
import scaml.ScamlCodeGenerator
import java.net.URLClassLoader
import scala.collection.mutable.HashMap
import scala.util.control.Exception
import scala.compat.Platform
import ssp.{SspCodeGenerator, ScalaCompiler}
import util.IOUtil
import java.io.{StringWriter, PrintWriter, FileWriter, File}
import collection.immutable.TreeMap

/**
 * A TemplateEngine is used to compile and load Scalate templates.
 * The TemplateEngine takes care of setting up the Scala compiler
 * and caching compiled templates for quicker subsequent loads
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
   * Whether a custom classpath should be combined with the deduced classpath
   */
  var combinedClassPath = false

  /**
   *
   */
  var resourceLoader: ResourceLoader = new FileResourceLoader
  var codeGenerators: Map[String, CodeGenerator] = Map("ssp" -> new SspCodeGenerator, "scaml" -> new ScamlCodeGenerator)
  var filters: Map[String, Filter] = Map()

  private val attempt = Exception.ignoring(classOf[Throwable])
  
  // Attempt to load all the built in filters.. Some may not load do to missing classpath
  // dependencies.
  attempt( filters += "plain" -> PlainFilter )
  attempt( filters += "javascript"-> JavascriptFilter )
  attempt( filters += "escaped"->EscapedFilter )
  attempt( filters += "markdown"->MarkdownFilter )

  var layoutStrategy = new DefaultLayoutStrategy(this)


  lazy val compiler = new ScalaCompiler(bytecodeDirectory, classpath, combinedClassPath)

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

  /**
   * Hy default lets bind the context so we get to reuse its methods in a template
   */
  var bindings = Binding("context", classOf[RenderContext].getName, true, None, "val", false) :: Nil

  private val templateCache = new HashMap[String, CacheEntry]


  /**
   * Compiles the given SSP template text and returns the template
   */
  def compileSsp(text: String, extraBindings:List[Binding] = Nil):Template = {
    compileText("ssp", text, extraBindings)
  }

  /**
   * Compiles the given SSP template text and returns the template
   */
  def compileScaml(text: String, extraBindings:List[Binding] = Nil):Template = {
    compileText("scaml", text, extraBindings)
  }

  /**
   * Compiles the given text using the given extension (such as ssp or scaml for example to denote what parser to use)
   * and return the template
   */
  def compileText(extension: String, text: String, extraBindings:List[Binding] = Nil):Template = {
    val file = File.createTempFile("scalate", "." + extension)
    val writer = new FileWriter(file)
    writer.write(text)
    writer.close()
    compile(file.getAbsolutePath, extraBindings)
  }


  /**
   * Compiles a template file/URI without placing it in the template cache. Useful for temporary
   * tes.emplates or dynamically created template
   */
  def compile(uri: String, extraBindings:List[Binding] = Nil):Template = {
    compileAndLoad(uri, extraBindings, 0)._1
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
          try {
            // Try to load a pre-compiled template from the classpath
              cache(uri, loadPrecompiledEntry(uri, extraBindings))
          } catch {
            case _: Throwable =>
              // It was not pre-compiled... compile and load it.
              cache(uri, compileAndLoadEntry(uri, extraBindings))
          }

        // It was in the cache..
        case Some(entry) =>
          // check for staleness
          if (allowReload && entry.isStale)
            // re-compile it
            cache(uri, compileAndLoadEntry(uri, extraBindings))
          else
            // Cache entry is valid
            entry.template
      }
    }
  }

  /**
   * Invalidates any cached Templates
   */
  def invalidateCachedTemplates() = {
    templateCache.synchronized {
      templateCache.clear
    }
  }

  /**
   *  Renders the given template URI using the current layoutStrategy
   */
  def layout(uri: String, context: RenderContext, extraBindings:List[Binding]): Unit = {
    val template = load(uri, extraBindings)
    layout(template, context)
  }


  /**
   * Renders the given template using the current layoutStrategy
   */
  def layout(template: Template, context: RenderContext): Unit = {
    RenderContext.update(context)
    layoutStrategy.layout(template, context)
  }


  /**
   * Renders the given template URI returning the output
   */
  def layout(uri: String, attributes: Map[String,Any] = Map(), extraBindings:List[Binding] = Nil): String = {
    val template = load(uri, extraBindings)
    layout(template, attributes)
  }

  /**
   * Renders the given template returning the output
   */
  def layout(template: Template, attributes: Map[String,Any]): String = {
    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = new DefaultRenderContext(this, out)
    for ((key, value) <- attributes) {
      context.attributes(key) = value
    }
    layout(template, context)
    buffer.toString
  }

  // can't use multiple methods with default arguments so lets manually expand them here...
  def layout(uri: String, context: RenderContext): Unit = layout(uri, context, Nil)
  def layout(template: Template): String = layout(template, Map[String,Any]())



  private def loadPrecompiledEntry(uri:String, extraBindings:List[Binding]) = {
    val className = generator(uri).className(uri)
    val template = loadCompiledTemplate(className);
    if( allowCaching && allowReload) {
      // Even though the template was pre-compiled, it may go or is stale
      // We still need to parse the template to figure out it's dependencies..
      val code = generateScala(uri, extraBindings);
      val entry = CacheEntry(template, code.dependencies, lastModified(template.getClass))
      if( entry.isStale ) {
        // Throw an exception since we should not load stale pre-compiled classes.
        throw new StaleCacheEntryException(uri)
      }
      // Yay the template is not stale.  Lets use it.
      entry
    } else {
      // If we are not going to be cache reloading.. then we
      // don't need to do the extra work.
      CacheEntry(template, Set(), 0)
    }
  }

  private def compileAndLoadEntry(uri:String, extraBindings:List[Binding]) = {
    val (template, dependencies) = compileAndLoad(uri, extraBindings, 0)
    CacheEntry(template, dependencies, Platform.currentTime)
  }

  private def cache(uri:String, ce:CacheEntry) :Template = {
    if( allowCaching ) {
      templateCache += (uri -> ce)
    }
    ce.template
  }

  /**
   * Returns the source file of the template URI
   */
  def sourceFileName(uri: String) = {
    // Write the source code to file..
    // to avoid paths like foo/bar/C:/whatnot on windows lets mangle the ':' character
    new File(sourceDirectory, uri.replace(':', '_') + ".scala")
  }

  def classFileName(uri: String) = {
    // Write the source code to file..
    // to avoid paths like foo/bar/C:/whatnot on windows lets mangle the ':' character
    new File(sourceDirectory, uri.replace(':', '_') + ".scala")
  }

  private def compileAndLoad(uri: String, extraBindings: List[Binding], attempt: Int): (Template, Set[String]) = {
    var code:Code = null
    try {

      // Generate the scala source code from the template
      code = generateScala(uri, extraBindings)

      val sourceFile = sourceFileName(uri)
      sourceFile.getParentFile.mkdirs
      IOUtil.writeBinaryFile(sourceFile, code.source.getBytes("UTF-8"))

      // Compile the generated scala code
      compiler.compile(sourceFile)
      
      // Write the source map information to the class file
      val sourceMap = buildSourceMap(uri, sourceFile, code.positions)
      // println(sourceMap)
      val classFile = new File(bytecodeDirectory, code.className.replace('.', '/')+".class")
      storeSourceMap(classFile, sourceMap)

      // Load the compiled class and instantiate the template object
      val template = loadCompiledTemplate(code.className)

      (template, code.dependencies)
    } catch {
      // TODO: figure out why we sometimes get these InstantiationException errors that
      // go away if you redo
      case e: InstantiationException =>
        if (attempt == 0) {
          compileAndLoad(uri, extraBindings, 1)
        } else {
          throw new TemplateException(e.getMessage, e)
        }

      case e:CompilerException=>

        // Translate the scala error location info
        // to the template locations..
        def template_pos(pos:Position) = {
          pos match {
            case p:OffsetPosition => {
              val filtered = code.positions.filterKeys( code.positions.ordering.compare(_,p) <= 0 )
              if( filtered.isEmpty ) {
                null
              } else {
                val (key,value) = filtered.last
                // TODO: handle the case where the line is different too.
                val colChange = pos.column - key.column
                OffsetPosition(value.source, value.offset+colChange)
              }
            }
            case _=> null
          }
        }

        var newmessage = "Compilation failed:\n"
        val errors = e.errors.map {
          (olderror) =>
            val pos =  template_pos(olderror.pos)
            if( pos==null ) {
              newmessage += ":"+olderror.pos+" "+olderror.message+"\n"
              newmessage += olderror.pos.longString+"\n"
              olderror
            } else {
              newmessage += uri+":"+pos+" "+olderror.message+"\n"
              newmessage += pos.longString+"\n"
              CompilerError(uri, olderror.message, pos, olderror)
            }
        }
        e.printStackTrace
        throw new CompilerException(newmessage, errors)
      case e: InvalidSyntaxException =>
        e.template = uri
        throw e
      case e: TemplateException => throw e
      case e: Throwable => throw new TemplateException(e.getMessage, e)
    }
  }


  /**
   * Gets the code generator to use for the give uri string by looking up the uri's extension
   * in the the codeGenerators map.
   */
  private def generator(uri: String): CodeGenerator = {
    extension(uri) match {
      case Some(ext)=>
        generatorForExtension(ext)
      case None=>
        throw new TemplateException("Template file extension missing. Cannot determine which template processor to use.")
    }
  }

  private def extension(uri: String): Option[String] = {
    val t = uri.split("\\.")
    if (t.length < 2) {
      None
    } else {
      Some(t.last)
    }
  }

  /**
   * Returns the code generator for the given file extension
   */
  private def generatorForExtension(extension: String) = codeGenerators.get(extension) match {
    case None => throw new TemplateException("Not a template file extension (" + codeGenerators.keysIterator.mkString("|") + "), you requested: " + extension);
    case Some(generator) => generator
  }

  private def loadCompiledTemplate(className:String) = {
    val cl = new URLClassLoader(Array(bytecodeDirectory.toURI.toURL), classLoader)
    val clazz = cl.loadClass(className)
    clazz.asInstanceOf[Class[Template]].newInstance
  }

  /**
   * Figures out the modification time of the class.
   */
  private def lastModified(clazz:Class[_]):Long = {
    val codeSource = clazz.getProtectionDomain.getCodeSource;
    if( codeSource !=null && codeSource.getLocation.getProtocol == "file") {
      val location = new File(codeSource.getLocation.getPath)
      if( location.isDirectory ) {
        val classFile = new File(location, clazz.getName.replace('.', '/')+".class")
        if( classFile.exists ) {
          return classFile.lastModified
        }
      } else {
        // class is inside an archive.. just use the modification time of the jar
        return location.lastModified
      }
    }
    // Bail out
    return 0
  }


  def buildSourceMap(uri:String, scalaFile:File, positions:TreeMap[OffsetPosition,OffsetPosition]) = {

    // Pretend to be a JSP for now..     
    // val stratum = extension(uri).get.toUpperCase
    val stratum = "JSP"

    var rc = "SMAP\n"
    rc += scalaFile.getName+"\n";
    rc += stratum+"\n"
    rc += "*S "+stratum+"\n"
    rc += "*F\n" 
    rc += "+ 0 "+uri.split("/").last+"\n"
    rc += uri.stripPrefix("/")+"\n"
    rc += "*L\n"

    // build a map of input-line -> List( output-line )
    var smap = new TreeMap[Int,List[Int]]()
    positions.foreach {
      case (out,in)=>
        var outs = out.line :: smap.getOrElse(in.line, Nil)
        smap += in.line -> outs
    }
    // sort the output lines..
    smap = smap.transform { (x,y)=> y.sortWith(_<_) }

    // the smap encoding support specifying ranges
    // to compress the data down a bit..
    // for now just do it the dumb way
    smap.foreach{
      case (in, outs)=>
//      outs.foreach {
//        out=>
//        rc += in+":"+out+"\n"
//      }
        rc += in+",1:"+outs.last+"\n"
    }
    rc += "*E\n"
    rc
  }

  def storeSourceMap(classFile:File, sourceMap:String) = {
    // Load the ASM ClassNode
    val cn = new ClassNode();
    val cr = new ClassReader( IOUtil.loadBinaryFile(classFile) )
    cr.accept(cn, 0);

    cn.sourceDebug = sourceMap

    // Store the ASM ClassNode
    val cw = new ClassWriter(0);
    cn.accept(cw);
    IOUtil.writeBinaryFile(classFile, cw.toByteArray())
  }
}
