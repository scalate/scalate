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

import filter._
import layout.{NullLayoutStrategy, LayoutStrategy}
import mustache.MustacheCodeGenerator
import scaml.ScamlCodeGenerator
import ssp.SspCodeGenerator
import support._
import util._

import scala.util.parsing.input.{OffsetPosition, Position}
import scala.collection.mutable.HashMap
import scala.collection.immutable.TreeMap
import scala.util.control.Exception
import scala.compat.Platform
import java.net.URLClassLoader
import java.io.{StringWriter, PrintWriter, FileWriter, File}
import xml.NodeSeq


/**
 * A TemplateEngine is used to compile and load Scalate templates.
 * The TemplateEngine takes care of setting up the Scala compiler
 * and caching compiled templates for quicker subsequent loads
 * of a requested template.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateEngine extends Logging {

  private case class CacheEntry(template: Template, dependencies: Set[String], timestamp: Long) {
    def isStale() = dependencies.exists {
      resourceLoader.lastModified(_) > timestamp
    }
  }

  /**
   * Whether or not markup sensitive characters for HTML/XML elements like &amp; &gt; &lt; are escaped or not
   */
  var escapeMarkup = true

  /**
   * Set to false if you don't want the template engine to ever cache any of the compiled templates.
   */
  var allowCaching = "true" == System.getProperty("scalate.allowCaching", "true")

  /**
   * If true, then the template engine will check to see if the template has been updated since last compiled
   * so that it can be reloaded.  Defaults to true.  YOu should set to false in production environments since
   * the tempaltes should not be changing.
   */
  var allowReload = "true" == System.getProperty("scalate.allowReload", "true")

  /**
   * Whether a custom classpath should be combined with the deduced classpath
   */
  var combinedClassPath = false

  /**
   * Sets the import statements used in each generated template class
   */
  var importStatements: List[String] = List("import _root_.scala.collection.JavaConversions._")

  /**
   *
   */
  var resourceLoader: ResourceLoader = new FileResourceLoader
  var codeGenerators: Map[String, CodeGenerator] = Map("ssp" -> new SspCodeGenerator, "scaml" -> new ScamlCodeGenerator, "moustache" -> new MustacheCodeGenerator)
  var filters: Map[String, Filter] = Map()

  private val attempt = Exception.ignoring(classOf[Throwable])
  
  // Attempt to load all the built in filters.. Some may not load do to missing classpath
  // dependencies.
  attempt( filters += "plain" -> PlainFilter )
  attempt( filters += "javascript"-> JavascriptFilter )
  attempt( filters += "escaped"->EscapedFilter )
  attempt( filters += "markdown"->MarkdownFilter )

  var layoutStrategy: LayoutStrategy = NullLayoutStrategy


  lazy val compiler = new ScalaCompiler(bytecodeDirectory, classpath, combinedClassPath)

  def sourceDirectory = new File(workingDirectory, "src")
  def bytecodeDirectory = new File(workingDirectory, "classes")

  var classpath: String = null
  
  private var _workingDirectory: File = null

  def workingDirectory: File = {
    // Use a temp working directory if none is configured.
    if( _workingDirectory == null ) {
      val value = System.getProperty("scalate.workdir", "")
      if (value != null && value.length > 0) {
        _workingDirectory = new File(value)
      }
      else {
        _workingDirectory = new File(new File(System.getProperty("java.io.tmpdir")), "_scalate");
      }
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
  private var _cacheHits = 0
  private var _cacheMisses = 0


  /**
   * Compiles the given Moustache template text and returns the template
   */
  def compileMoustache(text: String, extraBindings:List[Binding] = Nil):Template = {
    compileText("moustache", text, extraBindings)
  }

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
    IOUtil.writeText(file, text)
    compile(TemplateSource.fromFile(file), extraBindings)
  }


  /**
   * Compiles a template source without placing it in the template cache. Useful for temporary
   * templates or dynamically created template
   */
  def compile(source: TemplateSource, extraBindings:List[Binding] = Nil):Template = {
    compileAndLoad(source, extraBindings, 0)._1
  }

  /**
   * Generates the Scala code for a template.  Useful for generating scala code that
   * will then be compiled into the application as part of a build process.
   */
  def generateScala(source: TemplateSource, extraBindings:List[Binding] = Nil) = {
    generator(source.uri).generate(this, source, bindings ::: extraBindings)
  }


  /**
   * Generates the Scala code for a template.  Useful for generating scala code that
   * will then be compiled into the application as part of a build process.
   */
  def generateScala(uri: String, extraBindings:List[Binding]): Code = {
    generateScala(uriToSource(uri), extraBindings)
  }

  /**
   * Generates the Scala code for a template.  Useful for generating scala code that
   * will then be compiled into the application as part of a build process.
   */
  def generateScala(uri: String): Code = {
    generateScala(uriToSource(uri))
  }

  /**
   * The number of times a template load request was serviced from the cache.
   */
  def cacheHits = templateCache.synchronized { _cacheHits }


  /**
   * The number of times a template load request could not be serviced from the cache
   * and was loaded from disk.
   */
  def cacheMisses = templateCache.synchronized { _cacheMisses }

  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(source: TemplateSource, extraBindings:List[Binding]= Nil): Template = {
    templateCache.synchronized {

      // on the first load request, check to see if the INVALIDATE_CACHE JVM option is enabled
      if ( _cacheHits==0 && _cacheMisses==0 && java.lang.Boolean.getBoolean("org.fusesource.scalate.INVALIDATE_CACHE") ) {
        // this deletes generated scala and class files.
        invalidateCachedTemplates
      }

      // Determine whether to build/rebuild the template, load existing .class files from the file system,
      // or reuse an existing template that we've already loaded
      templateCache.get(source.uri) match {

        // Not in the cache..
        case None =>
          _cacheMisses += 1
          try {
            // Try to load a pre-compiled template from the classpath
              cache(source, loadPrecompiledEntry(source, extraBindings))
          } catch {
            case _: Throwable =>
              // It was not pre-compiled... compile and load it.
              cache(source, compileAndLoadEntry(source, extraBindings))
          }

        // It was in the cache..
        case Some(entry) =>
          // check for staleness
          if (allowReload && entry.isStale) {
            // Cache entry is stale, re-compile it
            _cacheMisses += 1
            cache(source, compileAndLoadEntry(source, extraBindings))
          } else {
            // Cache entry is valid
            _cacheHits += 1
            entry.template
          }
      }
    }
  }


  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(file: File, extraBindings: List[Binding]): Template = {
    load(TemplateSource.fromFile(file), extraBindings)
  }


  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(file: File): Template = {
    load(TemplateSource.fromFile(file))
  }


  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(uri: String, extraBindings: List[Binding]): Template = {
    load(uriToSource(uri), extraBindings)
  }


  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(uri: String): Template = {
    load(uriToSource(uri))
  }

  /**
   * Returns true if the URI can be loaded as a template
   */
  def canLoad(source: TemplateSource, extraBindings:List[Binding]= Nil): Boolean = {
    try {
      load(source, extraBindings) != null
    } catch {
      case e: ResourceNotFoundException => false
    }
  }


  /**
   * Returns true if the URI can be loaded as a template
   */
  def canLoad(uri: String): Boolean = {
    canLoad(uriToSource(uri))
  }

  /**
   * Returns true if the URI can be loaded as a template
   */
  def canLoad(uri: String, extraBindings:List[Binding]): Boolean = {
    canLoad(uriToSource(uri), extraBindings)
  }


  /**
   *  Invalidates all cached Templates.
   */
  def invalidateCachedTemplates() = {
    templateCache.synchronized {
      templateCache.clear
      IOUtil.rdelete(sourceDirectory)
      IOUtil.rdelete(bytecodeDirectory)
      sourceDirectory.mkdirs
      bytecodeDirectory.mkdirs
    }
  }


  // Layout as text methods
  //-------------------------------------------------------------------------


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
    val context = createRenderContext(out)
    for ((key, value) <- attributes) {
      context.attributes(key) = value
    }
    layout(template, context)
    buffer.toString
  }

  // can't use multiple methods with default arguments so lets manually expand them here...
  def layout(uri: String, context: RenderContext): Unit = layout(uri, context, Nil)
  def layout(template: Template): String = layout(template, Map[String,Any]())

  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource): String = layout(source, Map[String,Any]())
  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource, attributes: Map[String,Any]): String = {
    val template = load(source)
    layout(template, attributes)
  }
  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource, context: RenderContext, extraBindings:List[Binding]): Unit = {
    val template = load(source, extraBindings)
    layout(template, context)
  }

  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource, context: RenderContext): Unit = {
    val template = load(source)
    layout(template, context)
  }





  // Layout as markup methods
  //-------------------------------------------------------------------------

  /**
   * Renders the given template URI returning the output
   */
  def layoutAsNodes(uri: String, attributes: Map[String,Any] = Map(), extraBindings:List[Binding] = Nil): NodeSeq = {
    val template = load(uri, extraBindings)
    layoutAsNodes(template, attributes)
  }

  /**
   * Renders the given template returning the output
   */
  def layoutAsNodes(template: Template, attributes: Map[String,Any]): NodeSeq = {
    // TODO there is a much better way of doing this by adding native NodeSeq
    // support into the generated templates - especially for Scaml!
    // for now lets do it a crappy way...

    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = createRenderContext(out)
    for ((key, value) <- attributes) {
      context.attributes(key) = value
    }
    //layout(template, context)
    context.captureNodeSeq(template)
  }

  // can't use multiple methods with default arguments so lets manually expand them here...
  def layoutAsNodes(template: Template): NodeSeq = layoutAsNodes(template, Map[String,Any]())


  /**
   * Factory method used by the layout helper methods that should be overloaded by template engine implementations
   * if they wish to customize the render context implementation
   */
  protected def createRenderContext(out: PrintWriter): RenderContext = new DefaultRenderContext(this, out)

  private def loadPrecompiledEntry(source: TemplateSource, extraBindings:List[Binding]) = {
    val uri = source.uri
    val className = generator(uri).className(uri)
    val template = loadCompiledTemplate(className);
    if( allowCaching && allowReload && resourceLoader.exists(source.uri) ) {
      // Even though the template was pre-compiled, it may go or is stale
      // We still need to parse the template to figure out it's dependencies..
      val code = generateScala(source, extraBindings);
      val entry = CacheEntry(template, code.dependencies, lastModified(template.getClass))
      if( entry.isStale ) {
        // Throw an exception since we should not load stale pre-compiled classes.
        throw new StaleCacheEntryException(source)
      }
      // Yay the template is not stale.  Lets use it.
      entry
    } else {
      // If we are not going to be cache reloading.. then we
      // don't need to do the extra work.
      CacheEntry(template, Set(), 0)
    }
  }

  private def compileAndLoadEntry(source:TemplateSource, extraBindings:List[Binding]) = {
    val (template, dependencies) = compileAndLoad(source, extraBindings, 0)
    CacheEntry(template, dependencies, Platform.currentTime)
  }

  private def cache(source: TemplateSource, ce:CacheEntry) :Template = {
    if( allowCaching ) {
      templateCache += (source.uri -> ce)
    }
    ce.template
  }

  /**
   * Returns the source file of the template URI
   */
  protected def sourceFileName(uri: String) = {
    // Write the source code to file..
    // to avoid paths like foo/bar/C:/whatnot on windows lets mangle the ':' character
    new File(sourceDirectory, uri.replace(':', '_') + ".scala")
  }

  protected def classFileName(uri: String) = {
    // Write the source code to file..
    // to avoid paths like foo/bar/C:/whatnot on windows lets mangle the ':' character
    new File(sourceDirectory, uri.replace(':', '_') + ".scala")
  }

  protected val sourceMapLog = Logging(getClass, "SourceMap")

  private def compileAndLoad(source: TemplateSource, extraBindings: List[Binding], attempt: Int): (Template, Set[String]) = {
    var code: Code = null
    try {
      val uri = source.uri

      // Generate the scala source code from the template
      val g = generator(uri);
      code = g.generate(this, source, bindings ::: extraBindings)

      val sourceFile = sourceFileName(uri)
      sourceFile.getParentFile.mkdirs
      IOUtil.writeBinaryFile(sourceFile, code.source.getBytes("UTF-8"))

      // Compile the generated scala code
      compiler.compile(sourceFile)
      
      // Write the source map information to the class file
      val sourceMap = buildSourceMap(g.stratumName, uri, sourceFile, code.positions)

      sourceMapLog.debug("installing:" + sourceMap)

      storeSourceMap(new File(bytecodeDirectory, code.className.replace('.', '/')+".class"), sourceMap)
      storeSourceMap(new File(bytecodeDirectory, code.className.replace('.', '/')+"$.class"), sourceMap)

      // Load the compiled class and instantiate the template object
      val template = loadCompiledTemplate(code.className)

      (template, code.dependencies)
    } catch {
      // TODO: figure out why we sometimes get these InstantiationException errors that
      // go away if you redo
      case e: InstantiationException =>
        if (attempt == 0) {
          compileAndLoad(source, extraBindings, 1)
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
            val uri = source.uri
            val pos =  template_pos(olderror.pos)
            if( pos==null ) {
              newmessage += ":"+olderror.pos+" "+olderror.message+"\n"
              newmessage += olderror.pos.longString+"\n"
              olderror
            } else {
              newmessage += uri+":"+pos+" "+olderror.message+"\n"
              newmessage += pos.longString+"\n"
              // TODO should we pass the source?
              CompilerError(uri, olderror.message, pos, olderror)
            }
        }
        error(e)
        throw new CompilerException(newmessage, errors)
      case e: InvalidSyntaxException =>
        e.source = source
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


  protected def buildSourceMap(stratumName:String, uri:String, scalaFile:File, positions:TreeMap[OffsetPosition,OffsetPosition]) = {
    val shortName = uri.split("/").last
    val longName = uri.stripPrefix("/")

    val stratum: SourceMapStratum = new SourceMapStratum(stratumName)
    val fileId = stratum.addFile(shortName, longName)

    // build a map of input-line -> List( output-line )
    var smap = new TreeMap[Int,List[Int]]()
    positions.foreach {
      case (out,in)=>
        var outs = out.line :: smap.getOrElse(in.line, Nil)
        smap += in.line -> outs
    }
    // sort the output lines..
    smap = smap.transform { (x,y)=> y.sortWith(_<_) }

    smap.foreach{
      case (in, outs)=>
      outs.foreach {
        out=>
        stratum.addLine(in, fileId, 1, out, 1)
      }
    }
    stratum.optimize

    var sourceMap: SourceMap = new SourceMap
    sourceMap.setOutputFileName(scalaFile.getName)
    sourceMap.addStratum(stratum, true)
    sourceMap.toString
  }

  protected def storeSourceMap(classFile:File, sourceMap:String) = {
    SourceMapInstaller.store(classFile, sourceMap)
  }

  /**
   * Creates a {@link TEmplateSource} from a URI
   */
  protected def uriToSource(uri: String) = TemplateSource.fromUri(uri, resourceLoader)

}
