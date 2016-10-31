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

import _root_.com.google.inject.Key
import _root_.com.google.inject.servlet.ServletModule
import _root_.javax.servlet.http.HttpServlet

/**
 * This helper class provides a workaround for using 'with' in the Guice Servlet DSL
 *
 * @version $Revision : 1.1 $
 */

class RichBuilder(builder: ServletModule.ServletKeyBindingBuilder) {
  def by[T <: HttpServlet](servletKey: Class[T]) { builder.`with`(servletKey) };

  def by[T <: HttpServlet](
    servletKey: Class[T], params: java.util.Map[String, String]
  ) { builder.`with`(servletKey, params) };

  def by[T <: HttpServlet](servletKey: Key[T]) { builder.`with`(servletKey) };

  def by[T <: HttpServlet, S](
    servletKey: Key[T],
    params: java.util.Map[String, String]
  ) { builder.`with`(servletKey, params) };

  def withClass[T <: HttpServlet](servletKey: Class[T]) {
    builder.`with`(servletKey)
  };

  def withClass[T <: HttpServlet](
    servletKey: Class[T],
    params: java.util.Map[String, String]
  ) { builder.`with`(servletKey, params) };

  def withClass[T <: HttpServlet](servletKey: Key[T]) {
    builder.`with`(servletKey)
  };

  def withClass[T <: HttpServlet](
    servletKey: Key[T],
    params: java.util.Map[String, String]
  ) { builder.`with`(servletKey, params) };

}

