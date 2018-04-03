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
package org.fusesource.scalate.page

import org.fusesource.scalate.RenderContext
import org.fusesource.scalate.util.Files
import org.fusesource.scalate.util.IOUtil._
import java.io.File
import java.util.Date

trait Node {
  def title: String

  def createdAt: Date
}

/**
 * Represents a regular file which has no metadata other than of the file itself
 */
class FileNode(
  file: File) extends Node {

  def title = Files.dropExtension(file).replace('-', ' ').split("\\s+").map(_.capitalize).mkString(" ")

  def createdAt = new Date(file.lastModified)

}

object Node {

  implicit def toNode(context: RenderContext, file: File): Node = {
    if (file.extension == "page") {
      PageFilter.parse(context, file)
    } else {
      new FileNode(file)
    }
  }
}
