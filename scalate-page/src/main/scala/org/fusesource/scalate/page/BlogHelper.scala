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
package org.fusesource.scalate
package page

import org.fusesource.scalate.util.IOUtil._
import java.io.File
import util.Log

object BlogHelper {
  val log = Log(getClass); import log._

  /**
   * Returns the blog posts from the current request's directory by default sorted in date order
   */
  def posts: List[Page] = {
    val context = RenderContext()

    val base = context.requestUri.replaceFirst("""/?[^/]+$""", "")
    val dir = context.engine.resourceLoader.resource(base+"/index.page").flatMap(_.toFile).getOrElse(throw new Exception("index page not found.")).getParentFile

    println("Using dir: "+dir+" at request path: "+base)

    val index = new File(dir, "index.page")
    dir.descendants.filter(f => f != index && !f.isDirectory && f.name.endsWith(".page")).map { file =>
      val page = PageFilter.parse(context, file)
      page.link = file.relativeUri(dir).stripSuffix(".page") + ".html"
      page
    }.toList.sortBy(_.createdAt.getTime * -1)
  }

}