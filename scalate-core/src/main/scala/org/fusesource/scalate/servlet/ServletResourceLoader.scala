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

import java.io.File
import java.net.MalformedURLException
import javax.servlet.ServletContext

import org.fusesource.scalate.util.Resource._
import org.fusesource.scalate.util.{ Log, ResourceNotFoundException, ResourceLoader, FileResourceLoader }

object ServletResourceLoader extends Log {
  def apply(context: ServletContext) = new ServletResourceLoader(context)
}
import ServletResourceLoader._

/**
 * Loads files using <code>ServletContext</code>.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ServletResourceLoader(
  context: ServletContext,
  delegate: ResourceLoader = FileResourceLoader()) extends ResourceLoader {

  override def resource(uri: String) = {
    val file = realFile(uri)
    if (file != null) {
      if (file.isFile)
        Some(fromFile(file))
      else
        None
    } else {
      try {
        val url = context.getResource(uri)
        if (url != null) {
          val resource = fromURL(url)
          Some(resource)
        } else {
          delegate.resource(uri)
        }
      } catch {
        case x: MalformedURLException =>
          delegate.resource(uri)
      }
    }
  }

  /**
   * Returns the real path for the given uri.
   */
  def realPath(uri: String): String = {
    // TODO should ideally use the Resource API as then we could try figure out
    // the actual file for URL based resources not using getRealPath
    // (which has issues sometimes with unexpanded WARs and overlays)
    for (r <- resource(uri); f <- r.toFile if f != null) {
      return f.getPath
    }
    val file = realFile(uri)
    if (file != null) file.getPath else null
  }

  override protected def createNotFoundException(uri: String) = {
    new ResourceNotFoundException(resource = uri, root = context.getRealPath("/"))
  }

  /**
   * Returns the File for the given uri
   */
  protected def realFile(uri: String): File = {
    def findFile(uri: String): File = {
      val path = context.getRealPath(uri)
      debug("realPath for: " + uri + " is: " + path)

      var answer: File = null
      if (path != null) {
        val file = new File(path)
        debug("file from realPath for: " + uri + " is: " + file)
        if (file.canRead) { answer = file }
      }
      answer
    }

    findFile(uri) match {
      case file: File => file
      case _ => if (uri.startsWith("/") && !uri.startsWith("/WEB-INF")) {
        findFile("/WEB-INF" + uri)
      } else {
        null
      }
    }
  }

}
