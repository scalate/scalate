package org.fusesource.scalate

import support.ResourceLoader
import java.io.File
import util.IOUtil

/**
 * Helper methods to create a {@link TemplateSource} from various sources
 */
object TemplateSource {

  def fromText(uri: String, templateText: String) = StringTemplateSource(uri, templateText)

  def fromUri(uri: String, resourceLoader: ResourceLoader) = UriTemplateSource(uri, resourceLoader)

  def fromFile(file: File) = FileTemplateSource(file)
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

