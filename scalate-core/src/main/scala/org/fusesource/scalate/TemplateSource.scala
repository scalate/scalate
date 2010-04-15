package org.fusesource.scalate

import support.ResourceLoader
import java.io.File
import util.IOUtil
import java.net.{URL, URI}
import io.Source

/**
 * Helper methods to create a {@link TemplateSource} from various sources
 */
object TemplateSource {

  /**
   * Creates a {@link TemplateSource} from the actual String contents using the given
   * URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromText(uri: String, templateText: String) = StringTemplateSource(uri, templateText)

  /**
   * Creates a {@link TemplateSource} from a local URI such as in a web application using the
   * class loader to resolve URIs to actual resources
   */
  def fromUri(uri: String, resourceLoader: ResourceLoader) = UriTemplateSource(uri, resourceLoader)

  /**
   * Creates a {@link TemplateSource} from a file
   */
  def fromFile(file: File): FileTemplateSource = FileTemplateSource(file)

  /**
   * Creates a {@link TemplateSource} from a file name
   */
  def fromFile(fileName: String): FileTemplateSource = fromFile(new File(fileName))

  /**
   * Creates a {@link TemplateSource} from a URL
   */
  def fromURL(url: URL): URLTemplateSource = URLTemplateSource(url)

  /**
   * Creates a {@link TemplateSource} from a URL
   */
  def fromURL(url: String): URLTemplateSource = fromURL(new URL(url))

  /**
   * Creates a {@link TemplateSource} from the {@link Source} and the given URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromSource(uri: String, source: Source) = SourceTemplateSource(uri, source)
}

/**
 * Represents the source of a template
 *
 * @version $Revision: 1.1 $
 */
abstract class TemplateSource {
  def uri: String
  def text: String
}

case class StringTemplateSource(uri: String, text: String) extends TemplateSource

case class UriTemplateSource(uri: String, resourceLoader: ResourceLoader) extends TemplateSource {
  def text = resourceLoader.load(uri)
}

case class FileTemplateSource(file: File) extends TemplateSource {
  def uri = file.getPath

  def text = IOUtil.loadTextFile(file)
}

case class URLTemplateSource(url: URL) extends TemplateSource {
  def uri = url.toExternalForm

  def text = IOUtil.loadText(url.openStream)
}

case class SourceTemplateSource(uri: String, source: Source) extends TemplateSource {
  def text = {
    val builder = new StringBuilder
    for (c <- source) {
      builder.append(c)
    }
    builder.toString
  }
}

