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
import http.{HttpServletRequestWrapper, HttpServletResponse, HttpServletRequest}
import org.fusesource.scalate.util.Logging
import java.lang.String

/**
 * Servlet filter which auto routes to the scalate engines for paths which have a scalate template
 * defined.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateEngineFilter extends Filter with Logging {


  var config: FilterConfig = _
  var engine: ServletTemplateEngine = _
  var templateDirectories = List("/WEB-INF", "")
  var replacedExtensions = List(".html", ".htm")
  var errorUris: List[String] = ServletHelper.errorUris()
  
  /**
   * Called by the servlet engine to create the template engine and configure this filter
   */
  def init(filterConfig: FilterConfig) = {
    config = filterConfig
    engine = createTemplateEngine(config)
    filterConfig.getInitParameter("replaced-extensions") match {
      case null =>
      case x =>
        replacedExtensions = x.split(":+").toList
    }

    // register the template engine so they can be easily resolved from elsewhere
    ServletTemplateEngine(filterConfig.getServletContext) = engine
  }

  /**
   * Called by the servlet engine on shut down.
   */
  def destroy = {
  }

  /**
   * Allow derived filters to override and customize the template engine from the configuration
   */
  protected def createTemplateEngine(config: FilterConfig): ServletTemplateEngine = {
    new ServletTemplateEngine(config)
  }

  lazy val extensions = engine.extensions

  def find_template(path:String) = {

    // Is the uri a direct path to a template??
    // i.e: /path/page.jade -> /path/page.jade
    def find_direct(uri:String=path):Option[String] = {
      for( base <-templateDirectories; ext <- extensions) {
        val path = base + uri
        if( path.endsWith(ext) && engine.resourceLoader.exists(path) ) {
          return Some(path)
        }
      }
      return None
    }

    // Lets try to find the template by appending a template extension to the path
    // i.e: /path/page.html -> /path/page.html.jade
    def find_appended(uri:String=path):Option[String] = {
      for( base <-templateDirectories; ext <- extensions) {
        val path = base + uri + "." + ext
        if( engine.resourceLoader.exists(path) ) {
          return Some(path)
        }
      }
      return None
    }

    // Lets try to find the template by replacing the extension
    // i.e: /path/page.html -> /path/page.jade
    def find_replaced():Option[String] = {
      replacedExtensions.foreach{ ext=>
        if( path.endsWith(ext) ) {
          val rc = find_appended(path.stripSuffix(ext))
          if( rc != None )
            return rc
        }
      }
      None
    }

    find_direct().orElse(find_appended().orElse(find_replaced()))

  }


  /**
   * 
   */
  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    (request,response) match {
      case (request: HttpServletRequest, response: HttpServletResponse) =>
        val request_wrapper = wrap(request)

        debug("Checking '%s'".format(request.getRequestURI))
        find_template(request.getRequestURI.substring(request.getContextPath.length)) match {
          case Some(template)=>

            debug("Rendering '%s' using template '%s'".format(request.getRequestURI, template))
            val context = new ServletRenderContext(engine, request_wrapper, response, config.getServletContext)

            try {
              context.include(template, true)
            } catch {
              case e:Throwable => showErrorPage(request_wrapper, response, e)
            }

          case None=>
            chain.doFilter(request_wrapper, response)
        }
      
      case _ =>
        chain.doFilter(request, response)
    }
  }

  def showErrorPage(request: HttpServletRequest, response: HttpServletResponse, e:Throwable):Unit = {

    info("failure",e)

    // we need to expose all the errors property here...
    request.setAttribute("javax.servlet.error.exception", e)
    request.setAttribute("javax.servlet.error.exception_type", e.getClass)
    request.setAttribute("javax.servlet.error.message", e.getMessage)
    request.setAttribute("javax.servlet.error.request_uri", request.getRequestURI)
    request.setAttribute("javax.servlet.error.servlet_name", request.getServerName)
    request.setAttribute("javax.servlet.error.status_code", 500)
    response.setStatus(500)

    errorUris.find( x=>find_template(x).isDefined ) match {
      case Some(template)=>
        val context = new ServletRenderContext(engine, request, response, config.getServletContext)
        try {
          context.include(template, true)
        } catch {
          case _ =>
            throw e;
        }
      case None =>
        throw e;
    }
  }

  def wrap(request: HttpServletRequest) = new ScalateServletRequestWrapper(request) 

  class ScalateServletRequestWrapper(request: HttpServletRequest) extends HttpServletRequestWrapper(request) {
    override def getRequestDispatcher(path: String) = {
      find_template(path).map( new ScalateRequestDispatcher(_) ).getOrElse( request.getRequestDispatcher(path) )
    }
  }

  class ScalateRequestDispatcher(template:String) extends RequestDispatcher {
    def forward(request: ServletRequest, response: ServletResponse):Unit = include(request, response)
    def include(request: ServletRequest, response: ServletResponse):Unit = {
      (request,response) match {
        case (request: HttpServletRequest, response: HttpServletResponse) =>
          val context = new ServletRenderContext(engine, wrap(request), response, config.getServletContext)
          context.include(template, true)
        case _ =>
          None
      }
    }
  }

}
