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
package org.fusesource.scalate

import java.net.URI
import java.io.{File, FileInputStream, StringWriter, InputStreamReader}
import util.IOUtil

/**
 * Used by the template engine to load the content of templates.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
trait ResourceLoader {

  val pageFileEncoding = "UTF-8"

  def load( uri: String ): String
  
  def lastModified(uri:String): Long
  
  def resolve( base: String, path: String ): String

  def exists(uri: String): Boolean

}

class FileResourceLoader(val root:Option[File]=None) extends ResourceLoader {

  override def exists(uri: String): Boolean = {
    toFile(uri).exists
  }
  
  override def load(uri: String): String = {
    val file = toFileOrFail(uri);
    val reader = new InputStreamReader(new FileInputStream(file), pageFileEncoding)
    val writer = new StringWriter(file.length.asInstanceOf[Int]);
    try {
      IOUtil.copy(reader, writer)
      writer.toString
    } finally {
      reader.close
    }
  }

  override def lastModified(uri:String) = toFileOrFail(uri).lastModified

  override def resolve( base: String, path: String ): String = {
    if( path.startsWith( "/" ) )
      path
    else
      new URI( base ).resolve( path ).toString
  }

  protected def toFile(uri:String):File = {
    root match {
      case Some(dir)=> new File(dir, uri);
      case None=> new File(uri)
    }
  }

  protected def toFileOrFail(uri:String):File = {
    var file = toFile(uri)
    if (!file.canRead) {
      // lets try the ClassLoader
      var url = Thread.currentThread.getContextClassLoader.getResource(uri)
      if (url == null) {
        url = getClass.getClassLoader.getResource(uri)
      }
      if (url != null) {
        val fileName = url.getFile
        if (fileName != null) {
          file = new File(fileName)
        }
      }
    }
    if (!file.canRead) {
      throw new ResourceNotFoundException(uri)
    }
    file
  }
}

