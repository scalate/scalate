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

import java.util.Enumeration

import javax.servlet.{ FilterConfig, ServletConfig, ServletContext }

import scala.language.implicitConversions

object Config {
  implicit def servletConfig2Config(servletConfig: ServletConfig) = new Config {
    def getName = servletConfig.getServletName
    def getServletContext = servletConfig.getServletContext
    def getInitParameter(name: String) = servletConfig.getInitParameter(name)
    def getInitParameterNames = servletConfig.getInitParameterNames
  }

  implicit def filterConfig2Config(filterConfig: FilterConfig) = new Config {
    def getName = filterConfig.getFilterName
    def getServletContext = filterConfig.getServletContext
    def getInitParameter(name: String) = filterConfig.getInitParameter(name)
    def getInitParameterNames = filterConfig.getInitParameterNames
  }
}

/**
 * Provides a unified view of a ServletConfig or a FilterConfig.
 */
trait Config {
  def getName: String
  def getServletContext: ServletContext
  def getInitParameter(name: String): String
  def getInitParameterNames: Enumeration[_]
}
