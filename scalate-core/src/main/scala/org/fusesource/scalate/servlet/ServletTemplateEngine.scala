/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import _root_.org.fusesource.scalate.layout.DefaultLayoutStrategy
import org.fusesource.scalate.{Binding, TemplateEngine}
import org.fusesource.scalate.util.ClassPathBuilder
import java.io.File
import scala.tools.nsc.Global
import javax.servlet.{FilterConfig, ServletContext, ServletConfig};

object ServletTemplateEngine {
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
    }
    else {
      answer.asInstanceOf[ServletTemplateEngine]
    }
  }

  /**
   * Updates the current template engine - called on initialisation of the [[org.fusesource.scalate.TemplateEngineServlet]]
   */
  def update(servletContext: ServletContext, templateEngine: ServletTemplateEngine) {
    servletContext.setAttribute(templateEngineKey, templateEngine)
  }
}

/**
 * A Sevlet based TemplateEngine which initializes itself using a ServletConfig or a FilterConfig.
 *
 * The default layout files searched if no layout attribute is defined by a template are:
 *   * "WEB-INF/scalate/layouts/default.jade"
 *   * "WEB-INF/scalate/layouts/default.mustache"
 *   * "WEB-INF/scalate/layouts/default.scaml"
 *   * "WEB-INF/scalate/layouts/default.ssp"
 *  *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ServletTemplateEngine(val config: Config) extends TemplateEngine {
  bindings = List(Binding("context", classOf[ServletRenderContext].getName, true, isImplicit = true))
  classpath = buildClassPath
  resourceLoader = new ServletResourceLoader(config.getServletContext)
  layoutStrategy = new DefaultLayoutStrategy(this, TemplateEngine.templateTypes.map("/WEB-INF/scalate/layouts/default." + _):_*)

  info("Scalate template engine using working directory: " + workingDirectory)
  
  private def buildClassPath(): String = {

    val builder = new ClassPathBuilder

    // Add optional classpath prefix via web.xml parameter
    builder.addEntry(config.getInitParameter("compiler.classpath.prefix"))

    // Add containers class path
    builder.addPathFrom(getClass)
            .addPathFrom(classOf[ServletConfig])
            .addPathFrom(classOf[Product])
            .addPathFrom(classOf[Global])

    // Always include WEB-INF/classes and all the JARs in WEB-INF/lib just in case
    builder.addClassesDir(config.getServletContext.getRealPath("/WEB-INF/classes"))
            .addLibDir(config.getServletContext.getRealPath("/WEB-INF/lib"))

    // Add optional classpath suffix via web.xml parameter
    builder.addEntry(config.getInitParameter("compiler.classpath.suffix"))

    builder.classPath
  }
}
