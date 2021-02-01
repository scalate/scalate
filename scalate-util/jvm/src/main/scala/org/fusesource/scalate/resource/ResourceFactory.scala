package org.fusesource.scalate.resource

import java.io.File
import java.net.URL
import scala.io.Source

trait ResourceFactory {
  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a local URI such as in a web application using the
   * class loader to resolve URIs to actual resources
   */
  def fromUri(uri: String, resourceLoader: ResourceLoader) = UriResource(uri, resourceLoader)

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a file
   */
  def fromFile(file: File): FileResource = fromFile(file, file.getPath)

  def fromFile(file: File, uri: String): FileResource = FileResource(file, uri)

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a file name
   */
  def fromFile(fileName: String): FileResource = fromFile(new File(fileName))

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a URL
   */
  def fromURL(url: URL): URLResource = URLResource(url)

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a URL
   */
  def fromURL(url: String): URLResource = fromURL(new URL(url))

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from the [[scala.io.Source]] and the given URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromSource(uri: String, source: Source) = SourceResource(uri, source)
}
