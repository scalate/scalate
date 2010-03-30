package org.fusesource.scalate.guice

import _root_.com.google.inject.servlet.ServletModule
import _root_.com.google.inject.{Injector, Provides, Singleton}
import _root_.com.sun.jersey.api.core.{PackagesResourceConfig, DefaultResourceConfig, ResourceConfig}
import _root_.com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import _root_.javax.servlet.http.HttpServlet
import _root_.org.fusesource.scalate.servlet.TemplateEngineServlet
import _root_.java.util.HashMap

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

  override def configureServlets = {
    applyScalateServlets
    applyJerseyFilter
  }

  // TODO these could come from the TemplateEngine?
  var scalateServletUris = List("*.ssp", "*.scaml")

  protected def applyScalateServlets = {
    val servlet = classOf[TemplateEngineServlet]
    bind(servlet).in(classOf[Singleton])

    for (u <- scalateServletUris) {
      serveWith(u, classOf[TemplateEngineServlet])
    }
  }

  protected def applyJerseyFilter = filter("/*").through(classOf[GuiceContainer])

  @Provides @Singleton
  def createGuiceContainer(injector: Injector): GuiceContainer = {
    val container = new ScalateGuiceContainer(injector)
    println("created Guice configured container: " + container)
    container
  }

  @Provides @Singleton
  def createResourceConfig: ResourceConfig = {
    val map = new HashMap[String, AnyRef]()
    map.put("com.sun.jersey.config.property.packages", resourcePackageNames.mkString(";"))
    map.put("com.sun.jersey.config.property.WebPageContentRegex", webPageContentRegex)
    map.put("com.sun.jersey.config.feature.ImplicitViewables", "true")
    map.put("com.sun.jersey.config.feature.Redirect", "true")
    map.put("com.sun.jersey.config.feature.Trace", "true")

    val answer = new PackagesResourceConfig(map)
    //answer.setPropertiesAndFeatures(map)
    println("Created Guice configured resourceConfig: " + answer + " with properties: " + answer.getProperties)
    answer
  }

  // TODO demonstrate injection of the TemplateEngine??


  /**
   * The regular expression to find web content which should not be processed by the Jersey filter
   */
  def webPageContentRegex = ".+\\.(ssp|scaml)|(/(images|css|style|static)/.*)"

  /**
   * Returns a list of package names which are recursively scanned looking for JAXRS resource classes
   */
  def resourcePackageNames: List[String] = List("org.fusesource.scalate.console")

  /**
   * A helper method to avoid <a href="https://lampsvn.epfl.ch/trac/scala/ticket/3230">this compiler bug</a>
   * when using `with` or the RichBuilder (which uses `with`) inside a loop
   */
  protected def serveWith[T <: HttpServlet](urlPattern: String, aClass: Class[T]): Unit = serve(urlPattern).`with`(aClass)
}