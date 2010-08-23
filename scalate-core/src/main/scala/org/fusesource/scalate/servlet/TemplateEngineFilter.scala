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

import javax.servlet._
import http.{HttpServletResponse, HttpServletRequest}
import org.fusesource.scalate.util.Logging

/**
 * <p>
 * Servlet filter which auto routes to the scalate engines for paths which have a scalate template
 * defined.</p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateEngineFilter extends Filter with Logging {


  var config: FilterConfig = _
  var engine: ServletTemplateEngine = _
  var templateDirectories = List("/WEB-INF", "")

  /**
   * Place your application initialization code here.
   * Does nothing by default.
   */
  def init(filterConfig: FilterConfig) = {
    config = filterConfig
    engine = new ServletTemplateEngine(config)
  }

  /**
   * Place your application shutdown code here.
   * Does nothing by default.
   */
  def destroy = {
  }

  lazy val extensions:List[String] = {
    (engine.pipelines.keys.toList ::: engine.codeGenerators.keys.toList).distinct
  }

  /**
   * Instantiates a `CircumflexContext` object, binds it to current request,
   * consults `isProcessed, whether the request should be processed
   * and delegates to high-level equivalent
   * `doFilter(CircumflexContext, FilterChain)`
   * if necessary.
   */
  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain): Unit = {
    (req, res) match {
      case (req: HttpServletRequest, res: HttpServletResponse) =>
        def findTemplate():Option[String] = {

          val uri = req.getRequestURI.substring(req.getContextPath.length)


          for( base <-templateDirectories; ext <- extensions) {
            val path = base + uri + "." + ext
            if( engine.resourceLoader.exists(path) ) {
              return Some(path)
            }
          }
          return None
        }

        findTemplate match {
          case Some(template)=>
            info("Rendering '%s' using template '%s'".format(req.getRequestURI, template))

            val context = new ServletRenderContext(engine, req, res, config.getServletContext)
            res.setStatus(HttpServletResponse.SC_OK)
            context.include(template, true)

            return

          case _ =>
        }
      case _ =>
    }
    chain.doFilter(req, res)
  }

}
