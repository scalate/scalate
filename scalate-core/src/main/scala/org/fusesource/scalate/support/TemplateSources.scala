package org.fusesource.scalate.support

import org.fusesource.scalate.TemplateSource
import java.io.File
import java.net.{URL}
import io.Source


class StringTemplateSource(uri: String, text: String) extends StringResource(uri, text) with TemplateSource

class UriTemplateSource(uri: String, resourceLoader: ResourceLoader) extends UriResource(uri, resourceLoader) with TemplateSource

class FileTemplateSource(file: File) extends FileResource(file) with TemplateSource

class URLTemplateSource(url: URL) extends URLResource(url) with TemplateSource

class SourceTemplateSource(uri: String, source: Source) extends SourceResource(uri, source) with TemplateSource

class CustomExtensionTemplateSource(source: TemplateSource, extensionName: String) extends DelegateResource with TemplateSource {
  override def templateType = Some(extensionName)

  def delegate = source
}