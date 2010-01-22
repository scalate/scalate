/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package org.fusesource.ssp.sample

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
