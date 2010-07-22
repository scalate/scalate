/*
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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
      if (file != null && file.exists) {
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

