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
package org.fusesource.scalate.servlet

import java.io.File
import javax.servlet.ServletConfig
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
  import java.net.URLClassLoader
import org.fusesource.scalate.{PageContext, TemplateEngine}
import org.fusesource.scalate.util.Logging


abstract class ServerPageServlet extends HttpServlet with Logging {

  val templateEngine = new TemplateEngine

  override def init(config: ServletConfig) = {
    super.init(config)
    // Initialize the working directory, within which we'll write generated source code and .class files
    templateEngine.workingDirectoryRoot = new File(getServletContext.getRealPath("WEB-INF/_serverpages/"))
    templateEngine.workingDirectoryRoot.mkdirs
    templateEngine.classpath = buildClassPath
    templateEngine.resourceLoader = new ServletResourceLoader(getServletContext)
  }


  override def service(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    // Get our ducks in a row before we get started
    val uri = request.getServletPath
    val template = templateEngine.load(uri)
    template.renderPage(PageContext(response.getWriter, request, response, getServletContext))
  }


  def buildClassPath(): String = {
    val servletConfig = getServletConfig()
    val containerList = classLoaderList(getClass) ::: classLoaderList(classOf[HttpServlet])

    // Always include WEB-INF/classes and all the JARs in WEB-INF/lib just in case
    val classesDirectory = servletConfig.getServletContext.getRealPath("/WEB-INF/classes")
    val libDirectory = servletConfig.getServletContext.getRealPath("/WEB-INF/lib")
    val jars = findFiles(new File(libDirectory)).map {_.toString}

    // Allow adding a classpath prefix & suffix via web.xml
    val prefix = servletConfig.getInitParameter("compiler.classpath.prefix") match {
      case null => Nil
      case path: String => List(path)
    }
    val suffix = servletConfig.getInitParameter("compiler.classpath.suffix") match {
      case null => Nil
      case path: String => List(path)
    }

    // Put the pieces together

    // TODO we should probably be smart enough to filter out duplicates here...
    (prefix ::: containerList ::: classesDirectory :: jars ::: suffix ::: Nil).mkString(":")
  }


  private def classLoaderList[T](aClass: Class[T]): List[String] = {
    aClass.getClassLoader match {
      case u: URLClassLoader =>
        u.getURLs.toList.map {_.getFile}

      case _ => Nil
    }
  }

  private def findFiles(root: File): List[File] = {
    if (root.isFile)
      List(root)
    else
      makeList(root.listFiles).flatMap {f => findFiles(f)}
  }


  private def makeList(a: Array[File]): List[File] = {
    if (a == null)
      Nil
    else
      a.toList
  }


}
