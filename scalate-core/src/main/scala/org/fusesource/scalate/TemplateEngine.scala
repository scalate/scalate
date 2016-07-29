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
package org.fusesource.scalate

import filter._
import layout.{NullLayoutStrategy, LayoutStrategy}
import mustache.MustacheCodeGenerator
import osgi.BundleClassLoader
import scaml.ScamlCodeGenerator
import ssp.SspCodeGenerator
import jade.JadeCodeGenerator
import support._
import util._

import scala.util.parsing.input.{OffsetPosition, Position}
import scala.collection.immutable.TreeMap
import scala.util.control.Exception
import scala.compat.Platform
import java.net.URLClassLoader
import java.io.{StringWriter, PrintWriter, File}
import xml.NodeSeq
import collection.generic.TraversableForwarder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Callable, ConcurrentHashMap}

object TemplateEngine {
  val log = Log(getClass)

  def apply(sourceDirectories: Traversable[File],  mode: String): TemplateEngine = {
    new TemplateEngine(sourceDirectories, mode)
  }
  def apply(sourceDirectories: Traversable[File],  mode: String, templateCacheConcurrencyLevel: Int): TemplateEngine = {
    new TemplateEngine(sourceDirectories, mode, templateCacheConcurrencyLevel)
  }

  /**
   * The default template types available in Scalate
   */
  val templateTypes: List[String] = List("mustache", "ssp", "scaml", "jade")
}

/**
 * A TemplateEngine is used to compile and load Scalate templates.
 * The TemplateEngine takes care of setting up the Scala compiler
 * and caching compiled templates for quicker subsequent loads
 * of a requested template.
 *
 * The TemplateEngine uses a ''workingDirectory'' to store the generated scala source code and the bytecode. By default
 * this uses a dynamically generated directory. You can configure this yourself to use whatever directory you wish.
 * Or you can use the ''scalate.workdir'' system property to specify the workingDirectory 
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateEngine(var sourceDirectories: Traversable[File] = None, var mode: String = System.getProperty("scalate.mode", "production"), templateCacheConcurrencyLevel: Int = 4) {
  import TemplateEngine.log._

  private case class CacheEntry(template: Template, dependencies: Set[String], timestamp: Long) {
    def isStale() = timestamp!=0 && dependencies.exists {
      resourceLoader.lastModified(_) > timestamp
    }
  }

  /**
   * Whether or not markup sensitive characters for HTML/XML elements like &amp; &gt; &lt; are escaped or not
   */
  var escapeMarkup = true

  /**
   * Set to false if you don't want the template engine to ever cache any of the compiled templates.
   *
   * If not explicitly configured this property can be configured using the ''scalate.allowCaching'' system property
   */
  var allowCaching = "true" == System.getProperty("scalate.allowCaching", "true")

  /**
   * If true, then the template engine will check to see if the template has been updated since last compiled
   * so that it can be reloaded.  Defaults to true.  YOu should set to false in production environments since
   * the templates should not be changing.
   *
   * If not explicitly configured this property can be configured using the ''scalate.allowReload'' system property
   */
  var allowReload = "true" == System.getProperty("scalate.allowReload", "true")

  private var compilerInstalled = true

  /**
   * Whether a custom classpath should be combined with the deduced classpath
   */
  var combinedClassPath = false

  /**
   * Sets the import statements used in each generated template class
   */
  var importStatements: List[String] = List("import _root_.scala.collection.JavaConversions._",
    "import _root_.org.fusesource.scalate.support.TemplateConversions._",
    "import _root_.org.fusesource.scalate.util.Measurements._")

  /**
   * Loads resources such as the templates based on URIs
   */
  var resourceLoader: ResourceLoader = new FileResourceLoader(sourceDirectoriesForwarder)

  /**
   * A list of directories which are searched to load requested templates.
   */
  var templateDirectories = List("")
  var firstLoad = true

  var packagePrefix = ""

  var bootClassName = "scalate.Boot"
  var bootInjections: List[AnyRef] = List(this)

  private val booted = new AtomicBoolean()


  def boot: Unit = {
    if(booted.compareAndSet(false, true)) {

      if( allowReload ) {
        // Is the Scala compiler on the class path?
        try {
          getClass.getClassLoader.loadClass("scala.tools.nsc.settings.ScalaSettings")
        } catch {
          case e:Throwable =>
          // if it's not, then disable class reloading..
          debug("Scala compiler not found on the class path. Template reloading disabled.")
          allowReload = false
          compilerInstalled = false
        }
      }

      ClassLoaders.findClass(bootClassName, List(classLoader, Thread.currentThread.getContextClassLoader)) match {
        case Some(clazz) =>
          Boots.invokeBoot(clazz, bootInjections)

        case _ =>
          info("No bootstrap class " + bootClassName + " found on classloader: " + classLoader)
      }
    }
  }


  /**
   * A forwarder so we can refer to whatever the current latest value of sourceDirectories is even if the value
   * is mutated after the TemplateEngine is constructed
   */
  protected def sourceDirectoriesForwarder = {
    val engine = this
    new TraversableForwarder[File] {
      protected def underlying = engine.sourceDirectories
    }
  }

  /**
   * The supported template engines and their default extensions
   */
  var codeGenerators: Map[String, CodeGenerator] = Map("ssp" -> new SspCodeGenerator, "scaml" -> new ScamlCodeGenerator,
    "mustache" -> new MustacheCodeGenerator, "jade" -> new JadeCodeGenerator)
  
  var filters: Map[String, Filter] = Map.empty

  def filter(name:String) = codeGenerators.get(name).map( gen =>
        new Filter() {
          def filter(context: RenderContext, content: String) = {
            context.capture(compileText(name, content))
          }
        }
    ).orElse(filters.get(name))

  var pipelines: Map[String, List[Filter]] = Map.empty

  /**
   * Maps file extensions to possible template extensions for custom mappins such as for
   * Map("js" -> Set("coffee"), "css" => Set("sass", "scss"))
   */
  var extensionToTemplateExtension: collection.mutable.Map[String, collection.mutable.Set[String]] = collection.mutable.Map()

  /**
   * Returns the mutable set of template extensions which are mapped to the given URI extension.
   */
  def templateExtensionsFor(extension: String): collection.mutable.Set[String] = {
    extensionToTemplateExtension.getOrElseUpdate(extension, collection.mutable.Set())
  }

  private val attempt = Exception.ignoring(classOf[Throwable])

  /**
   * Returns the file extensions understood by Scalate; all the template engines and pipelines including
   * the wiki markup languages. 
   */
  def extensions: Set[String] = (codeGenerators.keySet ++ pipelines.keySet).toSet

  // Attempt to load all the built in filters.. Some may not load do to missing classpath
  // dependencies.
  attempt(filters += "plain" -> PlainFilter)
  attempt(filters += "javascript" -> JavascriptFilter)
  attempt(filters += "coffeescript" -> CoffeeScriptFilter)
  attempt(filters += "css" -> CssFilter)
  attempt(filters += "cdata" -> CdataFilter)
  attempt(filters += "escaped" -> EscapedFilter)

  attempt{
    CoffeeScriptPipeline(this)
  }

  var layoutStrategy: LayoutStrategy = NullLayoutStrategy

  lazy val compiler = createCompiler
  var compilerInitialized = false

  /**
   * Factory method to create a compiler for this TemplateEngine.
   * Override if you wish to contorl the compilation in a different way
   * such as in side SBT or something.
   */
  protected def createCompiler: Compiler = {
    compilerInitialized = true
    ScalaCompiler.create(this)
  }

  def shutdown() = if (compilerInitialized) compiler.shutdown

  def sourceDirectory = new File(workingDirectory, "src")
  def bytecodeDirectory = new File(workingDirectory, "classes")
  def libraryDirectory = new File(workingDirectory, "lib")
  def tmpDirectory = new File(workingDirectory, "tmp")

  var classpath: String = null
  
  private var _workingDirectory: File = null

  var classLoader = Thread.currentThread.getContextClassLoader match {
    case BundleClassLoader(_) => Thread.currentThread.getContextClassLoader()
    case _ => getClass().getClassLoader()
  }

  /**
   * By default lets bind the context so we get to reuse its methods in a template
   */
  var bindings = Binding("context", "_root_."+classOf[RenderContext].getName, true, None, "val", false) :: Nil
  
  val finderCache = new ConcurrentHashMap[String, String]
  private val templateCache: com.google.common.cache.Cache[String, CacheEntry] =
    com.google.common.cache.CacheBuilder.newBuilder()
      .concurrencyLevel(templateCacheConcurrencyLevel)
      .build()
  private val sysInvalidateCache = java.lang.Boolean.getBoolean("org.fusesource.scalate.INVALIDATE_CACHE")

  // Discover bits that can enhance the default template engine configuration. (like filters)
  ClassFinder.discoverCommands[TemplateEngineAddOn]("META-INF/services/org.fusesource.scalate/addon.index").foreach{ addOn =>
      debug("Installing Scalate add on " + addOn.getClass)
      addOn(this)
  }


  override def toString = getClass.getSimpleName + "(sourceDirectories: " + sourceDirectories + ")"

  /**
   * Returns true if this template engine is being used in development mode.
   */
  def isDevelopmentMode = mode != null && mode.toLowerCase.startsWith("d")

  /**
   * If not explicitly configured this will default to using the ''scalate.workdir'' system property to specify the
   * directory used for generating the scala source code and compiled bytecode - otherwise a temporary directory is used
   */
  def workingDirectory: File = {
    // Use a temp working directory if none is configured.
    if( _workingDirectory == null ) {
      val value = System.getProperty("scalate.workdir", "")
      if (value != null && value.length > 0) _workingDirectory = new File(value)
      else {
        val f = File.createTempFile("scalate-", "-workdir")
        // now lets delete the file so we can make a new directory there instead
        f.delete
        if (f.mkdirs) {
          _workingDirectory = f
          f.deleteOnExit
        } else {
          warn("Could not delete file %s so we could create a temp directory", f)
          _workingDirectory = new File(new File(System.getProperty("java.io.tmpdir")), "_scalate")
        }
      }
    }
    _workingDirectory
  }

  def workingDirectory_=(value:File) = {
    this._workingDirectory = value
  }


  /**
   * Compiles the given Moustache template text and returns the template
   */
  def compileMoustache(text: String, extraBindings:Traversable[Binding] = Nil):Template = {
    compileText("mustache", text, extraBindings)
  }

  /**
   * Compiles the given SSP template text and returns the template
   */
  def compileSsp(text: String, extraBindings:Traversable[Binding] = Nil):Template = {
    compileText("ssp", text, extraBindings)
  }

  /**
   * Compiles the given SSP template text and returns the template
   */
  def compileScaml(text: String, extraBindings:Traversable[Binding] = Nil):Template = {
    compileText("scaml", text, extraBindings)
  }

  /**
   * Compiles the given text using the given extension (such as ssp or scaml for example to denote what parser to use)
   * and return the template
   */
  def compileText(extension: String, text: String, extraBindings:Traversable[Binding] = Nil):Template = {
    tmpDirectory.mkdirs()
    val file = File.createTempFile("_scalate_tmp_", "." + extension, tmpDirectory)
    IOUtil.writeText(file, text)
    val loader = new FileResourceLoader(List(tmpDirectory))
    compile(TemplateSource.fromUri(file.getName, loader), extraBindings)
  }


  /**
   * Compiles a template source without placing it in the template cache. Useful for temporary
   * templates or dynamically created template
   */
  def compile(source: TemplateSource, extraBindings:Traversable[Binding] = Nil):Template = {
    compileAndLoad(source, extraBindings, 0)._1
  }

  /**
   * Generates the Scala code for a template.  Useful for generating scala code that
   * will then be compiled into the application as part of a build process.
   */
  def generateScala(source: TemplateSource, extraBindings:Traversable[Binding] = Nil) = {
    source.engine = this
    generator(source).generate(this, source, bindings ++ extraBindings)
  }


  /**
   * Generates the Scala code for a template.  Useful for generating scala code that
   * will then be compiled into the application as part of a build process.
   */
  def generateScala(uri: String, extraBindings:Traversable[Binding]): Code = {
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
  def cacheHits = templateCache.stats.hitCount()


  /**
   * The number of times a template load request could not be serviced from the cache
   * and was loaded from disk.
   */
  def cacheMisses = templateCache.stats.missCount()

  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(source: TemplateSource, extraBindings:Traversable[Binding]= Nil): Template = {
    source.engine = this
    // on the first load request, check to see if the INVALIDATE_CACHE JVM option is enabled
    if ( firstLoad && sysInvalidateCache ) {
      firstLoad = false
      invalidateCachedTemplates // this deletes generated scala and class files.
    }

    def recompile: CacheEntry = compileAndLoadEntry(source, extraBindings)
    def get: CacheEntry =
      try { // Try to load a pre-compiled template from the classpath
        val ce = loadPrecompiledEntry(source, extraBindings)
        debug("Loaded uri: " + source.uri + " template: " + ce.template)
        ce
      } catch { case _: Throwable =>
        val ce = recompile // It was not pre-compiled... compile and load it.
        debug("Loaded uri: " + source.uri + " template: " + ce.template)
        ce
      }

    var missed = false
    val entry =
      if (!allowCaching) get
      else
        try templateCache.get(source.uri, new Callable[CacheEntry]() { def call(): CacheEntry = { missed = true; get } } )
        catch { case ex: java.util.concurrent.ExecutionException => throw ex.getCause }

    if (missed || !allowReload || !entry.isStale) entry.template
    else { // Cache entry is stale, re-compile it
      val e = recompile
      templateCache.put(source.uri, e)
      e.template
    }
  }


  /**
   * Compiles and then caches the specified template.  If the template
   * was previously cached, the previously compiled template instance
   * is returned.  The cache entry in invalidated and then template
   * is re-compiled if the template file has been updated since
   * it was last compiled.
   */
  def load(file: File, extraBindings: Traversable[Binding]): Template = {
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
  def load(uri: String, extraBindings: Traversable[Binding]): Template = {
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
   * Returns a template source for the given URI and current resourceLoader
   */
  def source(uri: String): TemplateSource = TemplateSource.fromUri(uri, resourceLoader)

  /**
   * Returns a template source of the given type of template for the given URI and current resourceLoader 
   */
  def source(uri: String, templateType: String): TemplateSource = source(uri).templateType(templateType)

  /**
   * Returns true if the URI can be loaded as a template
   */
  def canLoad(source: TemplateSource, extraBindings:Traversable[Binding]= Nil): Boolean = {
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
  def canLoad(uri: String, extraBindings:Traversable[Binding]): Boolean = {
    canLoad(uriToSource(uri), extraBindings)
  }


  /**
   *  Invalidates all cached Templates.
   */
  def invalidateCachedTemplates() = {
    templateCache.synchronized {
      templateCache.invalidateAll()
      finderCache.clear
      IOUtil.recursiveDelete(sourceDirectory)
      IOUtil.recursiveDelete(bytecodeDirectory)
      sourceDirectory.mkdirs
      bytecodeDirectory.mkdirs
    }
  }


  // Layout as text methods
  //-------------------------------------------------------------------------


  /**
   *  Renders the given template URI using the current layoutStrategy
   */
  def layout(uri: String, context: RenderContext, extraBindings:Traversable[Binding]): Unit = {
    val template = load(uri, extraBindings)
    layout(template, context)
  }


  /**
   * Renders the given template using the current layoutStrategy
   */
  def layout(template: Template, context: RenderContext): Unit = {
    RenderContext.using(context) {
      val source = template.source
      if (source != null && source.uri != null) {
        context.withUri(source.uri) {
          layoutStrategy.layout(template, context)
        }
      }
      else {
        layoutStrategy.layout(template, context)
      }
    }
  }


  /**
   * Renders the given template URI returning the output
   */
  def layout(uri: String, attributes: Map[String,Any] = Map(), extraBindings: Traversable[Binding] = Nil): String = {
    val template = load(uri, extraBindings)
    layout(uri, template, attributes)
  }

  def layout(uri: String, out: PrintWriter, attributes: Map[String, Any]) {
    val template = load(uri)
    layout(uri, template, out, attributes)
  }

  protected def layout(uri: String, template: Template, out: PrintWriter, attributes: Map[String, Any]) {
    val context = createRenderContext(uri, out)
    for ((key, value) <- attributes) {
      context.attributes(key) = value
    }
    layout(template, context)
  }

  /**
   * Renders the given template returning the output
   */
  def layout(uri: String, template: Template, attributes: Map[String,Any]): String = {
    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    layout(uri, template, out, attributes)
    buffer.toString
  }

  // can't use multiple methods with default arguments so lets manually expand them here...
  def layout(uri: String, context: RenderContext): Unit = layout(uri, context, Nil)
  def layout(uri: String, template: Template): String = layout(uri, template, Map[String,Any]())

  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource): String = layout(source, Map[String,Any]())
  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource, attributes: Map[String,Any]): String = {
    val template = load(source)
    layout(source.uri, template, attributes)
  }
  /**
   *  Renders the given template source using the current layoutStrategy
   */
  def layout(source: TemplateSource, context: RenderContext, extraBindings:Traversable[Binding]): Unit = {
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
  def layoutAsNodes(uri: String, attributes: Map[String,Any] = Map(), extraBindings:Traversable[Binding] = Nil): NodeSeq = {
    val template = load(uri, extraBindings)
    layoutAsNodes(uri, template, attributes)
  }

  /**
   * Renders the given template returning the output
   */
  def layoutAsNodes(uri: String, template: Template, attributes: Map[String,Any]): NodeSeq = {
    // TODO there is a much better way of doing this by adding native NodeSeq
    // support into the generated templates - especially for Scaml!
    // for now lets do it a crappy way...

    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = createRenderContext(uri, out)
    for ((key, value) <- attributes) {
      context.attributes(key) = value
    }
    //layout(template, context)
    context.captureNodeSeq(template)
  }

  // can't use multiple methods with default arguments so lets manually expand them here...
  def layoutAsNodes(uri: String, template: Template): NodeSeq = layoutAsNodes(uri, template, Map[String,Any]())


  /**
   * Factory method used by the layout helper methods that should be overloaded by template engine implementations
   * if they wish to customize the render context implementation
   */
  protected def createRenderContext(uri: String, out: PrintWriter): RenderContext = new DefaultRenderContext(uri, this, out)

  private def loadPrecompiledEntry(source: TemplateSource, extraBindings:Traversable[Binding]) = {
    source.engine = this
    val uri = source.uri
    val className = source.className
    val template = loadCompiledTemplate(className, allowCaching)
    template.source = source
    if( allowCaching && allowReload && resourceLoader.exists(source.uri) ) {
      // Even though the template was pre-compiled, it may go or is stale
      // We still need to parse the template to figure out it's dependencies..
      val code = generateScala(source, extraBindings)
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
      CacheEntry(template, Set.empty, 0)
    }
  }

  private def compileAndLoadEntry(source:TemplateSource, extraBindings:Traversable[Binding]) = {
    val (template, dependencies) = compileAndLoad(source, extraBindings, 0)
    CacheEntry(template, dependencies, Platform.currentTime)
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

  protected val sourceMapLog = Log(getClass, "SourceMap")

  private def compileAndLoad(source: TemplateSource, extraBindings: Traversable[Binding], attempt: Int): (Template, Set[String]) = {
    source.engine = this
    var code: Code = null
    try {
      val uri = source.uri

      // Can we use a pipeline to process the request?
      pipeline(source) match {
        case Some(p)=>
          val template = new PipelineTemplate(p, source.text)
          template.source = source
          return (template, Set(uri))
        case _ =>
      }

      if( !compilerInstalled )
        throw new ResourceNotFoundException("Scala compiler not on the classpath.  You must either add it to the classpath or precompile all the templates")

      val g = generator(source)
      // Generate the scala source code from the template
      code = g.generate(this, source, bindings ++ extraBindings)

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
      template.source = source

      (template, code.dependencies)

    } catch {
      // TODO: figure out why we sometimes get these InstantiationException errors that
      // go away if you redo
      case e: InstantiationException =>
        if (attempt == 0) compileAndLoad(source, extraBindings, 1)
        else throw new TemplateException(e.getMessage, e)

      case e: CompilerException =>
        // Translate the scala error location info
        // to the template locations..
        def template_pos(pos:Position) = {
          pos match {
            case p:OffsetPosition =>
              val filtered = code.positions.filterKeys( code.positions.ordering.compare(_,p) <= 0 )
              if( filtered.isEmpty ) null
              else {
                val (key,value) = filtered.last
                // TODO: handle the case where the line is different too.
                val colChange = pos.column - key.column
                if ( colChange >= 0 ) OffsetPosition(value.source, value.offset+colChange)
                else pos
              }
            case _=> null
          }
        }

        var newmessage = "Compilation failed:\n"
        val errors = e.errors.map {
          (olderror) =>
            val uri = source.uri
            val pos = template_pos(olderror.pos)
            if ( pos==null ) {
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
        if (e.errors.isEmpty) throw e
        else throw new CompilerException(newmessage, errors)
      case e: InvalidSyntaxException =>
        e.source = source
        throw e
      case e: TemplateException => throw e
      case e: ResourceNotFoundException => throw e
      case e: Throwable => throw new TemplateException(e.getMessage, e)
    }
  }

  /**
   * Gets a pipeline to use for the give uri string by looking up the uri's extension
   * in the the pipelines map.
   */
  protected def pipeline(source: TemplateSource):Option[List[Filter]] = {
    //sort the extensions so we match the longest first.
    for( ext<- pipelines.keys.toList.sortWith{ case(x,y)=> if(x.length==y.length) x.compareTo(y)<0 else x.length > y.length } if source.uri.endsWith("."+ext) ) {
      return pipelines.get(ext)
    }
    None
  }




  /**
   * Gets the code generator to use for the give uri string by looking up the uri's extension
   * in the the codeGenerators map.
   */
  protected def generator(source: TemplateSource): CodeGenerator = {
    extension(source) match {
      case Some(ext)=>
        generatorForExtension(ext)
      case None=>
        throw new TemplateException("Template file extension missing. Cannot determine which template processor to use.")
    }
  }

  /**
   * Extracts the extension from the source's uri though derived engines could override this behaviour to
   * auto default missing extensions or performing custom mappings etc.
   */
  protected def extension(source: TemplateSource): Option[String] = source.templateType

  /**
   * Returns the code generator for the given file extension
   */
  protected def generatorForExtension(extension: String) = codeGenerators.get(extension) match {
    case None =>
      val extensions = pipelines.keySet.toList :::  codeGenerators.keySet.toList
      throw new TemplateException("Not a template file extension (" + extensions.mkString(" | ") + "), you requested: " + extension);
    case Some(generator) => generator
  }

  private def loadCompiledTemplate(className:String, from_cache:Boolean=true):Template = {
    val cl =
      if (from_cache) new URLClassLoader(Array(bytecodeDirectory.toURI.toURL), classLoader)
      else classLoader

    val clazz =
      try cl.loadClass(className)
      catch { case e:ClassNotFoundException =>
        if ( packagePrefix=="" ) throw e
        else {
          // Try without the package prefix.
          cl.loadClass(className.stripPrefix(packagePrefix).stripPrefix("."))
        }
    }

    clazz.asInstanceOf[Class[Template]].newInstance
  }

  /**
   * Figures out the modification time of the class.
   */
  private def lastModified(clazz:Class[_]):Long = {
    val codeSource = clazz.getProtectionDomain.getCodeSource
    if ( codeSource !=null && codeSource.getLocation.getProtocol == "file") {
      val location = new File(codeSource.getLocation.getPath)
      if( location.isDirectory ) {
        val classFile = new File(location, clazz.getName.replace('.', '/')+".class")
        if( classFile.exists ) return classFile.lastModified
      } else {
        // class is inside an archive.. just use the modification time of the jar
        return location.lastModified
      }
    }
    // Bail out
    0
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
        val outs = out.line :: smap.getOrElse(in.line, Nil)
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

    val sourceMap: SourceMap = new SourceMap
    sourceMap.setOutputFileName(scalaFile.getName)
    sourceMap.addStratum(stratum, true)
    sourceMap.toString
  }

  protected def storeSourceMap(classFile:File, sourceMap:String) = SourceMapInstaller.store(classFile, sourceMap)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a URI
   */
  protected def uriToSource(uri: String) = TemplateSource.fromUri(uri, resourceLoader)
}
