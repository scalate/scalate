package org.fusesource.scalate.support

import io.Source
import java.net.URL
import java.io._
import org.fusesource.scalate.util.IOUtil

/**
 * Represents a string, file or URI based resource
 */
trait Resource {

  /**
   * Returns the URI of the resource
   */
  def uri: String

  /**
   * Returns the text content of the resource
   */
  def text: String = IOUtil.loadText(inputStream)

  /**
   * Returns the reader of the content of the resource
   */
  def reader: Reader = new InputStreamReader(inputStream)

  /**
   * Returns the input stream of the content of the resource
   */
  def inputStream: InputStream

  /**
   * Returns the last modified time of the resource
   */
  def lastModified: Long
}

abstract class TextResource extends Resource {

  override def reader = new StringReader(text)

  def inputStream = new ByteArrayInputStream(text.getBytes)

  // just return current time as we have no way to know
  def lastModified: Long = System.currentTimeMillis
}

case class StringResource(uri: String, override val text: String) extends TextResource 

case class UriResource(override val uri: String, resourceLoader: ResourceLoader) extends DelegateResource {
  protected def delegate = resourceLoader.resourceOrFail(uri)
}

case class FileResource(file: File) extends Resource {
  def uri = file.getPath

  override def text = IOUtil.loadTextFile(file)

  override def reader = new FileReader(file)

  def inputStream = new FileInputStream(file)

  def lastModified = file.lastModified
}

case class URLResource(url: URL) extends Resource {
  def uri = url.toExternalForm

  def inputStream = url.openStream

  def lastModified = {
    val con = url.openConnection
    con.getLastModified
  }
}

case class SourceResource(uri: String, source: Source) extends TextResource {
  override def text = {
    val builder = new StringBuilder
    for (c <- source) {
      builder.append(c)
    }
    builder.toString
  }
}

abstract class DelegateResource extends Resource {
  override def uri = delegate.uri

  override def text = delegate.text

  override def reader = delegate.reader

  override def inputStream = delegate.inputStream

  def lastModified = delegate.lastModified

  protected def delegate: Resource
}

/**
 * Helper methods to create a  [[org.fusesource.scalate.support.Resource]] from various sources
 */
object Resource {

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from the actual String contents using the given
   * URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromText(uri: String, templateText: String) = StringResource(uri, templateText)

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a local URI such as in a web application using the
   * class loader to resolve URIs to actual resources
   */
  def fromUri(uri: String, resourceLoader: ResourceLoader) = UriResource(uri, resourceLoader)

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from a file
   */
  def fromFile(file: File): FileResource = FileResource(file)

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
