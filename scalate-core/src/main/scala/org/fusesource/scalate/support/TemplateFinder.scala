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

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.Files

/**
 * A helper object to find a template from a URI using a number of possible extensions and directories
 */
class TemplateFinder(engine: TemplateEngine) {

  var hiddenPatterns = List("""^_|/_""".r, """^\.|/\.""".r)
  var replacedExtensions = List(".html", ".htm")
  lazy val extensions = engine.extensions

  def findTemplate(path: String): Option[String] = {
    var rc = Option(engine.finderCache.get(path))
    if (rc.isEmpty) {
      rc = search(path)
      if (rc.isDefined && !engine.isDevelopmentMode) {
        engine.finderCache.put(path, rc.get)
      }
    }
    rc
  }

  def search(rootOrPath: String): Option[String] = {
    val path = if (rootOrPath == "/") "/index" else rootOrPath

    for (p <- hiddenPatterns; if p.findFirstIn(path).isDefined) {
      return None
    }

    // Is the uri a direct path to a template??
    // i.e: /path/page.jade -> /path/page.jade
    def findDirect(uri: String): Option[String] = {
      for (base <- engine.templateDirectories; ext <- extensions) {
        val path = base + uri
        if (path.endsWith(ext) && engine.resourceLoader.exists(path)) {
          return Some(path)
        }
      }
      return None
    }

    // Lets try to find the template by appending a template extension to the path
    // i.e: /path/page.html -> /path/page.html.jade
    def findAppended(uri: String): Option[String] = {
      for (base <- engine.templateDirectories; ext <- extensions) {
        val path = base + uri + "." + ext
        if (engine.resourceLoader.exists(path)) {
          return Some(path)
        }
      }
      return None
    }

    // Lets try to find the template by replacing the extension
    // i.e: /path/page.html -> /path/page.jade
    def findReplaced(): Option[String] = {
      replacedExtensions.foreach {
        ext =>
          if (path.endsWith(ext)) {
            val rc = findAppended(path.stripSuffix(ext))
            if (rc != None)
              return rc
          }
      }
      None
    }

    // Lets try to find the template for well known template extensions
    // i.e:
    // /path/page.css -> List(/path/page.sass, /path/page.scss)
    // /path/page.js -> List(/path/page.coffee)
    def findTemplateAlias(uri: String): Option[String] = {
      val ext = Files.extension(uri)
      lazy val remaining = path.stripSuffix(ext)
      if (ext.size > 0) {
        engine.extensionToTemplateExtension.get(ext) match {
          case Some(set) =>
            for (base <- engine.templateDirectories; ext <- set) {
              val path = base + remaining + ext
              if (engine.resourceLoader.exists(path)) {
                return Some(path)
              }
            }
            None
          case _ => None
        }
      }
      None
    }

    findDirect(path).orElse(findAppended(path).orElse(findTemplateAlias(path).orElse(findReplaced())))
  }
}