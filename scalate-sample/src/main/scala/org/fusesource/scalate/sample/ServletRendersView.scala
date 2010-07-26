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

package org.fusesource.scalate.sample

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletConfig
import org.fusesource.scalate.servlet.{ServletTemplateEngine, ServletRenderContext}
import org.fusesource.scalate.TemplateEngine

class ServletRendersView extends HttpServlet
{

  var templateEngine:TemplateEngine = null

  override def init(config: ServletConfig) = {
    super.init(config)
    templateEngine = new ServletTemplateEngine(config)
  }

  override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val model = new Person("Bob", "Mcwhatnot")
    val context = new ServletRenderContext(templateEngine, request, response, getServletContext)
    context.view(model)
  }

}