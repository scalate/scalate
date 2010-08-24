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

package org.fusesource.scalate.support

import org.fusesource.scalate.ResourceNotFoundException
import org.fusesource.scalate.util.Logging
import org.fusesource.scalate.support.Resource._
import java.net.URI
import java.io.File

/**
 * A strategy for loading [[Resource]] instances
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
trait ResourceLoader extends Logging {
  val pageFileEncoding = "UTF-8"

  def resource(uri: String): Option[Resource]

  def exists(uri: String): Boolean = resource(uri).isDefined

  def load(uri: String): String = resourceOrFail(uri).text

  def lastModified(uri: String): Long = resourceOrFail(uri).lastModified

  def resolve(base: String, path: String): String = {
    if (path.startsWith("/"))
      path
    else
      new URI(base).resolve(path).toString
  }

  def resourceOrFail(uri: String): Resource = resource(uri) match {
    case Some(r) =>
      debug("found resource: " + r)
      r
    case _ =>
      throw createNotFoundException(uri)
  }

  protected def createNotFoundException(uri: String) = new ResourceNotFoundException(uri)
}

case class FileResourceLoader(rootDir: Option[File] = None) extends ResourceLoader {
  def resource(uri: String): Option[Resource] = {
    debug("Trying to load uri: " + uri)

    var answer = false
    if (uri != null) {
      val file = toFile(uri)
      if (file != null && file.exists && file.isFile) {
        if (!file.canRead) {
          throw new ResourceNotFoundException(uri, description = "Could not read from " + file.getAbsolutePath)
        }
        return Some(fromFile(file))
      }

      // lets try the ClassLoader
      val relativeUri = uri.stripPrefix("/")
      var url = Thread.currentThread.getContextClassLoader.getResource(relativeUri)
      if (url == null) {
        url = getClass.getClassLoader.getResource(relativeUri)
      }
      if (url != null) {
        return Some(fromURL(url))
      }
    }
    None
  }

  protected def toFile(uri: String): File = {
    rootDir match {
      case Some(dir) => new File(dir, uri);
      case None => new File(uri)
    }
  }
}

