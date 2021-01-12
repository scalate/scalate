package org.fusesource.scalate.resource

import java.io.{ File, InputStream, InputStreamReader, Reader }

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
  def text: String

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

/**
 * Helper methods to create a  [[org.fusesource.scalate.support.Resource]] from various sources
 */
object Resource extends ResourceFactory {

  /**
   * Creates a [[org.fusesource.scalate.support.Resource]] from the actual String contents using the given
   * URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromText(uri: String, templateText: String) = StringResource(uri, templateText)

}
