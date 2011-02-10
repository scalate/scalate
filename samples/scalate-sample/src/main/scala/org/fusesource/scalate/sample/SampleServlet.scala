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

package org.fusesource.scalate.sample

import java.util.Date
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.compat.Platform

class SampleServlet extends HttpServlet
{
  override def doGet(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    // Perform the business logic
    var headerNames = Set.empty[String]
    val iterator = request.getHeaderNames
    while (iterator.hasMoreElements)
      headerNames += iterator.nextElement.toString

    // Attach a model representing the result to the request object
    request.setAttribute("foo", new Foo(request.getPathInfo, headerNames))
    request.setAttribute("timestamp", new Date(Platform.currentTime))

    // Delegate response rendering to the SSP

    val ssp = "/WEB-INF/ssp/attributes.ssp"
    println("Now forwarding to SSP: " + ssp)

    request.getRequestDispatcher(ssp).forward(request, response)
  }


}
