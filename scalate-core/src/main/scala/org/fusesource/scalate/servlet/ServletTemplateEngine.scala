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
package org.fusesource.scalate.servlet

import java.io.File

import javax.servlet.{ ServletConfig, ServletContext }
import org.fusesource.scalate.layout.{ DefaultLayoutStrategy, LayoutStrategy }
import org.fusesource.scalate.util._
import org.fusesource.scalate.{ Binding, TemplateEngine }

import scala.tools.nsc.Global

object ServletTemplateEngine {
  val log = Log(getClass)

  val templateEngineKey = classOf[ServletTemplateEngine].getName

  /**
   * Gets the current template engine
   *
   * @throws IllegalArgumentException if no template engine has been registered with the [[javax.servlet.ServletContext]]
   */
  def apply(servletContext: ServletContext): ServletTemplateEngine = {
    val answer = servletContext.getAttribute(templateEngineKey)
    if (answer == null) {
      throw new IllegalArgumentException("No ServletTemplateEngine instance registered on ServletContext for key " +
        templateEngineKey + ". Are you sure your web application has registered the Scalate TemplateEngineServlet?")
    } else {
      answer.asInstanceOf[ServletTemplateEngine]
    }
  }

  /**
   * Updates the current template engine - called on initialisation of the [[org.fusesource.scalate.servlet.TemplateEngineServlet]]
   */
  def update(servletContext: ServletContext, templateEngine: ServletTemplateEngine): Unit = {
    servletContext.setAttribute(templateEngineKey, templateEngine)
    // now lets fire the bootstrap code
    templateEngine.boot
  }

  /**
   * Configures the given TemplateEngine to use the default servlet style layout strategy.
   *
   * The default layout files searched if no layout attribute is defined by a template are:
   *   * "WEB-INF/scalate/layouts/default.jade"
   *   * "WEB-INF/scalate/layouts/default.mustache"
   *   * "WEB-INF/scalate/layouts/default.scaml"
   *   * "WEB-INF/scalate/layouts/default.ssp"
   */
  def setLayoutStrategy(engine: TemplateEngine): LayoutStrategy = {
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, TemplateEngine.templateTypes.map("/WEB-INF/scalate/layouts/default." + _): _*)
    engine.layoutStrategy
  }

  /**
   * Returns the source directories to use for the given config
   */
  def sourceDirectories(config: Config): List[File] = {
    config.getServletContext.getRealPath("/") match {
      case path: String => List(new File(path))
      case null => List()
    }
  }

}

/**
 * A Servlet based TemplateEngine which initializes itself using a ServletConfig or a FilterConfig.
 *
 * The default layout files searched if no layout attribute is defined by a template are:
 *   * "WEB-INF/scalate/layouts/default.jade"
 *   * "WEB-INF/scalate/layouts/default.mustache"
 *   * "WEB-INF/scalate/layouts/default.scaml"
 *   * "WEB-INF/scalate/layouts/default.ssp"
 *  *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ServletTemplateEngine(
  val config: Config) extends TemplateEngine(ServletTemplateEngine.sourceDirectories(config)) {
  import ServletTemplateEngine.log._

  templateDirectories ::= "/WEB-INF"
  bindings = List(Binding("context", "_root_." + classOf[ServletRenderContext].getName, true, isImplicit = true))
  classpath = buildClassPath
  classLoader = Thread.currentThread.getContextClassLoader
  resourceLoader = new ServletResourceLoader(config.getServletContext)
  ServletTemplateEngine.setLayoutStrategy(this)
  bootInjections = List(this, config.getServletContext)

  Option(config.getInitParameter("boot.class")).foreach(clazz => bootClassName = clazz)

  info("Scalate template engine using working directory: %s", workingDirectory)
  private def buildClassPath(): String = {

    val builder = new ClassPathBuilder

    // Add optional classpath prefix via web.xml parameter
    builder.addEntry(config.getInitParameter("compiler.classpath.prefix"))

    // Add containers class path
    builder.addPathFrom(getClass)
      .addPathFrom(classOf[ServletConfig])
      .addPathFrom(classOf[Product])

    try {
      builder.addPathFrom(classOf[Global])
    } catch {
      case x: Throwable => // the scala compiler might not be on the path.
    }

    // Always include WEB-INF/classes and all the JARs in WEB-INF/lib just in case
    builder
      .addClassesDir(config.getServletContext.getRealPath("/WEB-INF/classes"))
      .addLibDir(config.getServletContext.getRealPath("/WEB-INF/lib"))

    // Add optional classpath suffix via web.xml parameter
    builder.addEntry(config.getInitParameter("compiler.classpath.suffix"))

    builder.classPath
  }
}
