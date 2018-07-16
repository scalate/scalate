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
package org.fusesource.scalate.console

import java.io.File

/**
 * Represents a template archetype (namely a template used to generate other templates that can then be customized)
 *
 * @version $Revision: 1.1 $
 */
case class Archetype(file: File) {

  def uri = file.getName

  def name = file.getName

  /**
   * Returns the extension of the template archetype
   */
  def extension = {
    val i = uri.lastIndexOf('.')
    if (i > 0) {
      uri.substring(i + 1)
    } else {
      uri
    }
  }

  def archetype = file.getPath

  /**
   * Returns the URI to post to that generates the new template for this archetype
   */
  def createUri(newTemplatePrefix: String) = {
    "/scalate/createTemplate?name=" + newTemplatePrefix + "archetype=" + file.getPath
  }
}
