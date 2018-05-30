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
package org.fusesource.scalate
package camel

import java.io.File
import java.util.Map

import org.apache.camel.Endpoint
import org.apache.camel.impl.DefaultComponent
import org.springframework.core.io.DefaultResourceLoader
import org.fusesource.scalate.util.FileResourceLoader

/**
 * @version $Revision : 1.1 $
 */

class ScalateComponent() extends DefaultComponent {

  var defaultTemplateExtension: String = "ssp"
  var templateEngine: TemplateEngine = new TemplateEngine()

  var resourceLoader = new DefaultResourceLoader()

  templateEngine.resourceLoader = new FileResourceLoader() {

    override protected def toFile(uri: String): File = {
      resourceLoader.getResource(uri).getFile
    }
  }
  def createEndpoint(uri: String, remaining: String, parameters: Map[String, Object]): Endpoint = {
    new ScalateEndpoint(this, uri, remaining, defaultTemplateExtension)
  }
}
