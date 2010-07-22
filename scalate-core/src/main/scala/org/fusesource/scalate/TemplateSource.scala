package org.fusesource.scalate

import java.io.File
import support._
import java.net.URL
import io.Source

/**
 * Represents the source of a template
 *
 * @version $Revision : 1.1 $
 */
trait TemplateSource extends Resource {


  /**
   * Returns the type of the template (ssp, scaml, mustache etc).
   *
   * By default the extension is extracted from the uri but custom implementations
   * can override this so that a uri could be "foo.html" but the extension overriden to be "mustache"
   * for example
   */
  def templateType: Option[String] = {
    val t = uri.split("\\.")
    if (t.length < 2) {
      None
    } else {
      Some(t.last)
    }
  }

  /**
   * Returns a new TemplateSource which uses the given template type irrespective of the actual uri file extension
   *
   * For example this lets you load a TemplateSource then convert it to be
   * of a given fixed type of template as follows:
   *
   * <code>TemplateSource.fromFile("foo.txt").templateType("mustache")</code>
   */
  def templateType(extension: String) = new CustomExtensionTemplateSource(this, extension)
}

/**
 * Helper methods to create a [[org.fusesource.scalate.TemplateSource]] from various sources
 */
object TemplateSource {

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from the actual String contents using the given
   * URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromText(uri: String, templateText: String) = new StringTemplateSource(uri, templateText)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a local URI such as in a web application using the
   * class loader to resolve URIs to actual resources
   */
  def fromUri(uri: String, resourceLoader: ResourceLoader) = new UriTemplateSource(uri, resourceLoader)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a file
   */
  def fromFile(file: File): FileTemplateSource = new FileTemplateSource(file)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a file name
   */
  def fromFile(fileName: String): FileTemplateSource = fromFile(new File(fileName))

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a URL
   */
  def fromURL(url: URL): URLTemplateSource = new URLTemplateSource(url)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a URL
   */
  def fromURL(url: String): URLTemplateSource = fromURL(new URL(url))

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from the [[scala.io.Source]] and the given URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromSource(uri: String, source: Source) = new SourceTemplateSource(uri, source)
}