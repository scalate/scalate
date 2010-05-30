package org.fusesource.scalate.guice

import _root_.org.fusesource.scalate.servlet.TemplateEngineServlet
import _root_.com.google.inject.servlet.ServletModule
import _root_.com.google.inject.{Injector, Provides, Singleton}
import _root_.com.sun.jersey.api.core.{PackagesResourceConfig, DefaultResourceConfig, ResourceConfig}
import _root_.com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import _root_.javax.servlet.http.HttpServlet
import _root_.java.{ util => ju}
import _root_.scala.collection.JavaConversions._
import org.fusesource.scalate.TemplateEngine

/**
 * A default Guice  {@link ServletModule} which registers Jersey and the Scalate servlets
 *
 * @version $Revision : 1.1 $
 */
class ScalateModule extends ServletModule {

  /**
   * The implicit type conversion to avoid the 'with' method in the DSL
   */
  implicit def builderToRichBuilder(builder: ServletModule.ServletKeyBindingBuilder) = new RichBuilder(builder);

  /**
   * Configure any servlets or filters for the application
   */
  override def configureServlets = {
    applyScalateServlets
    applyJerseyFilter
  }

  var scalateServletUris: List[String] = TemplateEngine.templateTypes.map(s => "*." + s)

  /**
   * Registers the Scalate servlets
   */
  protected def applyScalateServlets = {
    val servlet = classOf[TemplateEngineServlet]
    bind(servlet).in(classOf[Singleton])

    for (u <- scalateServletUris) {
      serveWith(u, classOf[TemplateEngineServlet])
    }
  }

  /**
   * Registers the Jersey filter
   */
  protected def applyJerseyFilter = filter("/*").through(classOf[GuiceContainer])

  /**
   * Creates the {@link GuiceContainer} to configure Jersey
   */
  @Provides @Singleton
  def createGuiceContainer(injector: Injector): GuiceContainer = {
    val container = new ScalateGuiceContainer(injector)
    container
  }

  /**
   * Creates the resource configuration for the Jersey {@link GuiceContainer}
   */
  @Provides @Singleton
  def createResourceConfig: ResourceConfig = {
    // TODO shame there's not an easy way to go ju.Map -> Map in the standard library!
    val jumap = new ju.HashMap[String, AnyRef]()
    for ((k, v) <- createResourceConfigProperties) {
      jumap.put(k, v)
    }
    new PackagesResourceConfig(jumap)
  }

  /**
   * Creates the properties used to configure the {@link GuiceContainer}'s resource config in
   * {@link #createResourceConfig} for Jersey
   */
  @Provides @Singleton
  def createResourceConfigProperties: Map[String, AnyRef] = Map(
    "com.sun.jersey.config.property.packages" -> resourcePackageNames.mkString(";"),
    "com.sun.jersey.config.property.WebPageContentRegex" -> webPageContentRegex.mkString("|"),
    "com.sun.jersey.config.feature.ImplicitViewables" -> "true",
    "com.sun.jersey.config.feature.Redirect" -> "true",
    "com.sun.jersey.config.feature.Trace" -> "true"
  )


  // TODO demonstrate injection of the TemplateEngine??


  /**
   * The regular expression to find web content which should not be processed by the Jersey filter
   */
  def webPageContentRegex: List[String] = List(".+\\." + templateExtensions.mkString("(", "|", ")"), "/images/.*", "/css/.*", ".+\\.ico")

  /**
   * Returns a list of package names which are recursively scanned looking for JAXRS resource classes
   */
  def resourcePackageNames: List[String] = List("org.fusesource.scalate.console")

  /**
   * A helper method to avoid <a href="https://lampsvn.epfl.ch/trac/scala/ticket/3230">this compiler bug</a>
   * when using `with` or the RichBuilder (which uses `with`) inside a loop
   */
  protected def serveWith[T <: HttpServlet](urlPattern: String, aClass: Class[T]): Unit = serve(urlPattern).`with`(aClass)

  /**
   * Returns the default list of template extensions which are rendered directly with Scalate
   * rather than going through the Jersey filter
   */
  protected def templateExtensions: List[String] = TemplateEngine.templateTypes
}