/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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
package org.fusesource.scalate.servlet;

import javax.servlet.ServletConfig
import org.fusesource.scalate.{Binding, TemplateEngine}
import java.io.File
import org.fusesource.scalate.util.ClassLoaders._;

/**
 * A TemplateEngine which initializes itself using a ServletConfig
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ServletTemplateEngine(var config:ServletConfig) extends TemplateEngine {

  bindings = List(Binding("context", classOf[ServletRenderContext].getName, true))
  workingDirectory = new File(config.getServletContext.getRealPath("WEB-INF/_scalate/"))
  workingDirectory.mkdirs
  classpath = buildClassPath
  resourceLoader = new ServletResourceLoader(config.getServletContext)

  private def buildClassPath(): String = {

    val containerList = classLoaderList(getClass) ::: classLoaderList(classOf[ServletConfig])

    // Always include WEB-INF/classes and all the JARs in WEB-INF/lib just in case
    val classesDirectory = config.getServletContext.getRealPath("/WEB-INF/classes")
    val libDirectory = config.getServletContext.getRealPath("/WEB-INF/lib")
    val jars = findFiles(new File(libDirectory)).map {_.toString}

    // Allow adding a classpath prefix & suffix via web.xml
    val prefix = config.getInitParameter("compiler.classpath.prefix") match {
      case null => Nil
      case path: String => List(path)
    }
    val suffix = config.getInitParameter("compiler.classpath.suffix") match {
      case null => Nil
      case path: String => List(path)
    }

    // Put the pieces together

    // TODO we should probably be smart enough to filter out duplicates here...
    (prefix ::: containerList ::: classesDirectory :: jars ::: suffix ::: Nil).mkString(":")
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
