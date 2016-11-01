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
package org.fusesource.scalate.guice

import _root_.org.fusesource.scalate.servlet.TemplateEngineServlet
import _root_.com.google.inject.servlet.ServletModule
import _root_.com.google.inject.{ Injector, Provides, Singleton }
import _root_.com.sun.jersey.api.core.{ PackagesResourceConfig, DefaultResourceConfig, ResourceConfig }
import _root_.com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import _root_.javax.servlet.http.HttpServlet
import _root_.java.{ util => ju }
import _root_.scala.collection.JavaConversions._
import org.fusesource.scalate.TemplateEngine

/**
 * A default Guice [[com.google.inject.servlet.ServletModule]] which registers Jersey and the Scalate servlets
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

  var useJerseyUriRegex: Boolean = false

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
   * Creates the [[com.sun.jersey.guice.spi.container.servlet.GuiceContainer]] to configure Jersey
   */
  @Provides @Singleton
  def createGuiceContainer(injector: Injector): GuiceContainer = {
    val container = new ScalateGuiceContainer(injector)
    container
  }

  /**
   * Creates the resource configuration for the Jersey [[com.sun.jersey.guice.spi.container.servlet.GuiceContainer]]
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
   * Creates the properties used to configure the [[com.sun.jersey.guice.spi.container.servlet.GuiceContainer]]'s resource config in
   * {@link #createResourceConfig} for Jersey
   */
  @Provides @Singleton
  def createResourceConfigProperties: Map[String, AnyRef] = {
    val answer = Map(
      "com.sun.jersey.config.property.packages" -> resourcePackageNames.mkString(";"),

      "com.sun.jersey.config.feature.FilterForwardOn404" -> "true",

      "com.sun.jersey.config.feature.ImplicitViewables" -> "true",
      "com.sun.jersey.config.feature.Redirect" -> "true",
      "com.sun.jersey.config.feature.Trace" -> "true"
    )

    // as of Jersey 1.1.4-ea05 we don't need to mess with a regex
    // see: https://jersey.dev.java.net/issues/show_bug.cgi?id=485
    if (useJerseyUriRegex) {
      answer ++ Map("com.sun.jersey.config.property.WebPageContentRegex" -> webPageContentRegex.mkString("|"))
    } else {
      answer
    }
  }

  // TODO demonstrate injection of the TemplateEngine??

  /**
   * The regular expression to find web content which should not be processed by the Jersey filter.
   * This is only required until we can get the FilterForwardOn404 issue resolved.
   * See: https://jersey.dev.java.net/issues/show_bug.cgi?id=485
   */
  def webPageContentRegex: List[String] = {
    val extensions = templateExtensions ::: fileExtensionsExcludedFromJersey
    List(".+\\." + extensions.mkString("(", "|", ")"), "/images/.*", "/css/.*")
  }

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
   * Returns the list of file types which should be excluded from the Jersey filter
   * (until we can get the FilterForwardOn404 setting working) so that they are rendered correctly
   * using the servlet engine
   */
  protected def fileExtensionsExcludedFromJersey: List[String] = List("ico", "jpg", "jpeg", "gif", "png", "css", "js", "jscon")

  /**
   * Returns the default list of template extensions which are rendered directly with Scalate
   * rather than going through the Jersey filter
   */
  protected def templateExtensions: List[String] = TemplateEngine.templateTypes
}