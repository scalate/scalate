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
package org.fusesource.scalate.maven


import java.io.File
import java.net.{URLClassLoader, URL}


class ConfExportMojoSupport {

  def apply(mojo:ConfExportMojo) {
    import mojo._
    import scala.collection.JavaConversions._

    if (skip.toBoolean) { return }

    //
    // Lets use project's classpath when we run the site gen tool
    //
    val urls: Array[URL] = testClassPathElements.map { d =>
      new File(d.toString).toURI.toURL
    }.toArray

    getLog.debug("Found project class loader URLs: " + urls.toList)
    val loader = new URLClassLoader(urls, Thread.currentThread.getContextClassLoader)

    val oldLoader = Thread.currentThread.getContextClassLoader
    Thread.currentThread.setContextClassLoader(loader)
    try {
      import scala.language.reflectiveCalls      

      // Structural Typing FTW (avoids us doing manual reflection)
      type ConfluenceExport = {
        var url: String
        var space: String
        var target: File
        var user: String
        var password: String
        var allow_spaces: Boolean
        var format: String
        var target_db: Boolean
        def execute(p: String => Unit): AnyRef 
      }

      val className = "org.fusesource.scalate.tool.commands.ConfluenceExport"
      val exporter = loader.loadClass(className).newInstance.asInstanceOf[ConfluenceExport]

      exporter.url = mojo.url
      exporter.space = mojo.space
      exporter.target = mojo.target
      exporter.user = mojo.user
      exporter.password = mojo.password
      exporter.allow_spaces = mojo.allow_spaces.toBoolean
      exporter.format = mojo.format
      exporter.target_db = mojo.target_db.toBoolean
      exporter.execute(value => getLog.info(value) )

    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }

  }

}
