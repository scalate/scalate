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
package org.fusesource.scalate.resource

import org.fusesource.scalate.util.{ Files, IOUtil }
import slogging.LazyLogging

import slogging.StrictLogging

import io.Source
import java.io._
import java.net.{ URISyntaxException, URL }
import scala.io.Source

trait StreamResource extends Resource {
  /**
   * Returns the text content of the resource
   */
  override def text: String = IOUtil.loadText(inputStream)

  def toFile: Option[File] = None
}

/**
 * Not all resources are writeable so this optional trait is for those
 */
trait WriteableResource extends StreamResource {
  /**
   * Writes text to the resource replacing its previous content
   */
  def text_=(value: String): Unit = IOUtil.writeText(outputStream, value)

  /**
   * Returns the output stream of the resource
   */
  def outputStream: OutputStream

  /**
   * Returns the writer to the content of the resource
   */
  def writer = new OutputStreamWriter(outputStream)

}

case class UriResource(override val uri: String, resourceLoader: ResourceLoader) extends DelegateResource {
  protected def delegate = resourceLoader.resourceOrFail(uri).asInstanceOf[StreamResource]
}

/**
 * Can act as a RichFile type interface too, adding a number of extra helper methods to make Files more rich
 */
case class FileResource(file: File, uri: String) extends WriteableResource {
  override def text = IOUtil.loadTextFile(file)

  override def reader = new FileReader(file)

  def inputStream = new FileInputStream(file)

  def outputStream = new FileOutputStream(file)

  def lastModified = file.lastModified

  /**
   * Create a child file
   */
  def /(name: String) = FileResource(new File(file, name), uri + "/" + name)

  implicit def asFile: File = file

  override def toFile = Some(file)

  def name = file.getName

  /**
   * Returns the extension of the file
   */
  def extension = Files.extension(name)

  /**
   * Returns the name of the file without its extension
   */
  def nameDropExtension = Files.dropExtension(name)

  /**
   * Recursively finds the first file in this directory that matches the given
   * predicate or matches against this file for non-directories
   */
  def recursiveFind(f: File => Boolean): Option[File] = Files.recursiveFind(file)(f)

  /**
   * Returns an Iterable over the immediate children of this directory
   * or an empty Iterable
   */
  def children: Iterable[File] = Files.children(file)

  /**
   * Returns an Iterable over any descending files
   */
  def descendants: Iterable[File] = Files.descendants(file)

  /**
   * Returns an Iterable over this file and any descending files
   */
  def andDescendants: Iterable[File] = Files.andDescendants(file)

  /**
   * Returns the relative URI of this file from the given root directory
   */
  def relativeUri(root: File) = Files.relativeUri(root, file)
}

case class URLResource(url: URL) extends WriteableResource with StrictLogging {

  def uri = url.toExternalForm

  lazy val connection = url.openConnection

  def inputStream = url.openStream

  def outputStream = connection.getOutputStream

  def lastModified = {
    val con = url.openConnection
    con.getLastModified
  }

  override def toFile: Option[File] = {
    var f: File = null
    if (url.getProtocol == "file") {
      try {
        try {
          f = new File(url.toURI)
        } catch {
          case e: URISyntaxException => f = new File(url.getPath)
        }
      } catch {
        case e: ThreadDeath => throw e
        case e: VirtualMachineError => throw e
        case e: Exception => logger.debug(s"While converting $url to a File I caught: $e", e)
      }
    }
    if (f != null && f.exists && f.isFile) {
      Some(f)
    } else {
      None
    }
  }
}

case class SourceResource(uri: String, source: Source) extends TextResource {
  override def text = {
    val builder = new StringBuilder
    val s: Source = source.pos match {
      case 0 => source
      case _ => {
        source.close()
        source.reset()
      }
    }
    for (c <- s) {
      builder.append(c)
    }
    builder.toString
  }
}

abstract class DelegateResource extends StreamResource {
  override def uri = delegate.uri

  override def text = delegate.text

  override def reader: Reader = delegate.reader

  override def inputStream = delegate.inputStream

  def lastModified = delegate.lastModified

  protected def delegate: StreamResource
}
