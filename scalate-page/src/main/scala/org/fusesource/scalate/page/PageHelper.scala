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

package org.fusesource.scalate
package page

import org.fusesource.scalate.util.IOUtil._
import java.io.File
import util.Logging

object PageHelper {
  def defaultBlogDir = new File("src") / "main" / "webapp" / "blog"

}

/**
 * A helper class for working with pages
 */
class PageHelper(context: RenderContext) extends Logging {

  /**
   * Returns the blog posts from the current request's directory by default sorted in date order
   */
  //def blogPosts(dir: File = defaultBlogDir, max: Int = 10): List[Page] = {
  def blogPosts: List[Page] = {
    val dir = defaultBlogDir
    println("Using dir: " + dir.getCanonicalPath)
    val index = new File(dir, "index.page")

    dir.descendants.filter(f => f != index && !f.isDirectory && f.name.endsWith(".page")).map {
      file => PageFilter.parse(context, file)
    }.toList.sortBy(_.createdAt.getTime * -1)
  }

  protected def defaultBlogDir: File = {
    context.requestFile match {
      case Some(f) => f.getParentFile
      case _ =>
        warn("Could not find File for resource " + context.requestResource)
        PageHelper.defaultBlogDir
    }
  }
}