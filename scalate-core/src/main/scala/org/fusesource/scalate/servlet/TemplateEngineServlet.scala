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

import org.fusesource.scalate.TemplateEngine
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.{ ServletContext, ServletConfig }
import org.fusesource.scalate.util.{ Log }

object TemplateEngineServlet extends Log {

  protected var singleton: TemplateEngineServlet = _

  def apply(): TemplateEngineServlet = singleton

  def update(servlet: TemplateEngineServlet): Unit = singleton = servlet

  def render(
    template: String,
    templateEngine: TemplateEngine,
    servletContext: ServletContext,
    request: HttpServletRequest,
    response: HttpServletResponse): Unit = {
    val context = new ServletRenderContext(templateEngine, request, response, servletContext)

    if (template == null || template.length == 0 || template == "/") {
      // lets try find an index page if we are given an empty URI which sometimes happens
      // with jersey filter and guice servlet
      TemplateEngine.templateTypes.map("index." + _).find(u => servletContext.getResource(u) != null) match {
        case Some(name) =>
          servletContext.log("asked to resolve uri: " + template + " so delegating to: " + name)
          servletContext.getRequestDispatcher(name).forward(request, response)
        //context.include(name, true)

        case _ =>
          servletContext.log("No template available for: " + template)
          response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      }
    } else {
      context.include(template, true)
      // we should set the OK here as we might be forwarded from the Jersey
      // filter after it detected a 404 and found that there's no JAXRS resource at / or foo.ssp or something
      response.setStatus(HttpServletResponse.SC_OK)
    }
  }
}

/**
 * Servlet which renders the requested Scalate template.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateEngineServlet extends HttpServlet {
  var templateEngine: ServletTemplateEngine = _

  override def init(config: ServletConfig) {
    super.init(config)

    templateEngine = createTemplateEngine(config)

    // register the template engine and servlet so they can be easily resolved
    TemplateEngineServlet() = this
    ServletTemplateEngine(getServletContext) = templateEngine
  }

  /**
   * Allow derived servlets to override and customize the template engine from the configuration
   */
  protected def createTemplateEngine(config: ServletConfig): ServletTemplateEngine = {
    new ServletTemplateEngine(config)
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    render(request.getServletPath, request, response)
  }

  def render(template: String, request: HttpServletRequest, response: HttpServletResponse): Unit = {
    TemplateEngineServlet.render(template, templateEngine, getServletContext, request, response)
  }

}
