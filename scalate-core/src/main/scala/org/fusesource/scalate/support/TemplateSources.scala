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
package org.fusesource.scalate.support

import org.fusesource.scalate.TemplateSource
import java.io.File
import java.net.URL
import io.Source
import org.fusesource.scalate.util._

class StringTemplateSource(
  uri: String,
  text: String) extends StringResource(uri, text) with TemplateSource

class UriTemplateSource(
  uri: String,
  resourceLoader: ResourceLoader) extends UriResource(uri, resourceLoader) with TemplateSource

class FileTemplateSource(
  file: File,
  uri: String) extends FileResource(file, uri) with TemplateSource

class URLTemplateSource(
  url: URL) extends URLResource(url) with TemplateSource

class SourceTemplateSource(
  uri: String,
  source: Source) extends SourceResource(uri, source) with TemplateSource

class CustomExtensionTemplateSource(
  source: TemplateSource,
  extensionName: String) extends DelegateResource with TemplateSource {

  override def templateType = Some(extensionName)

  def delegate = source
}