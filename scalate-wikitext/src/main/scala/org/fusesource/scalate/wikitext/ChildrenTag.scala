/**
 * Copyright (C) 2009-2010 the original author or authors.
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
package wikitext

import StringConverter._

import java.io.File
import xml.NodeSeq
import util.{Log, Files}

object ChildrenTag extends Log
/**
 * Implements the **children** macro in confluence
 */
class ChildrenTag extends AbstractConfluenceTagSupport("children") {
  import ChildrenTag._
  var page: Option[String] = None
  var depth = 1
  var all = false

  def setOption(key: String, value: String) = key match {
    case "all" => all = asBoolean(value)
    case "depth" => depth = asInt(value)
    case "page" => page = Some(value)
    case _ => Blocks.unknownAttribute(key, value)
  }


  def doTag() = {
    val context = RenderContext()

    def showChildren(rootDir: File, dir: File, level: Int): NodeSeq = {
      // TODO allow different sorting
      // lets make sure directories for "foo" appear after the "foo.conf" file so use "/" which is after "."
      lazy val files = dir.listFiles().sortBy(f => if (f.isDirectory) f.getName + "/" else f.getName)
      if (all || level <= depth && files.nonEmpty) {
        <ul>
          {files.map {
          f =>
            if (f.isDirectory) {
              val dirXml = showChildren(rootDir, f, level + 1)
              if (dirXml.isEmpty)
                dirXml
              else
                <li>
                  {dirXml}
                </li>
            } else {
              val title = Pages.title(f)
              val link = Files.relativeUri(rootDir, f)
              <li>
                <a href={link}>
                  {title}
                </a>
              </li>
            }
        }}
        </ul>
      } else {
        Nil
      }
    }

    val requestUri = if (context.currentTemplate != null) context.currentTemplate else context.requestUri
    val idx = requestUri.lastIndexOf('/')
    val pageName = if (idx >= 0) requestUri.substring(idx+1) else requestUri
    val pageUri = page.getOrElse(Files.dropExtension(pageName))
    SwizzleLinkFilter.findWikiFile(pageUri) match {
      case Some(file) =>
        info("{children} now going to iterate from file '%s'",file)
        val rootDir = file.getParentFile
        val dir = new File(rootDir, Files.dropExtension(file))
        if (!dir.exists) {
          warn("{children} cannot find directory: %s", dir)
        }
        else {
          //context << showChildren(rootDir, dir, 1)
          builder.charactersUnescaped(showChildren(rootDir, dir, 1).toString)
        }

      case _ =>
        warn("Could not find wiki file for page '%s'", pageUri)
    }
  }
}
