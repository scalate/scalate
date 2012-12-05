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

import java.net.{URL, URLClassLoader}


/**
 * This goal precompiles the Scalate templates into classes to be included
 * in your build.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class PrecompileMojoSupport {

  def apply(mojo:PrecompileMojo) = {
    import mojo._;
    import scala.collection.JavaConversions._

    //
    // Lets use project's classpath when we run the pre-compiler tool
    //

    val urls: Array[URL] = classPathElements.map { d =>
      new File(d.toString).toURI.toURL
    }.toArray

    getLog.debug("Found project class loader URLs: " + urls.toList)
    val loader = new URLClassLoader(urls, Thread.currentThread.getContextClassLoader)

    val oldLoader = Thread.currentThread.getContextClassLoader
    Thread.currentThread.setContextClassLoader(loader)
    try {
      import scala.language.reflectiveCalls

      // Structural Typing FTW (avoids us doing manual reflection)
      type Precompiler = {
        var sources: Array[File]
        var workingDirectory: File
        var targetDirectory: File
        var templates: Array[String]
        var info: {def apply(v1:String):Unit}
        var contextClass: String
        var bootClassName:String
        def execute(): Unit
      }

      val precompilerClassName = "org.fusesource.scalate.support.Precompiler"
      val precompiler = loader.loadClass(precompilerClassName).newInstance.asInstanceOf[Precompiler]

      precompiler.info = (value:String)=>getLog.info(value)
      
      precompiler.sources = Array(mojo.warSourceDirectory, mojo.resourcesSourceDirectory)
      precompiler.workingDirectory = mojo.targetDirectory
      precompiler.targetDirectory = mojo.classesDirectory      
      precompiler.templates = mojo.templates.toList.toArray
      precompiler.contextClass = mojo.contextClass
      precompiler.bootClassName = mojo.bootClassName
      precompiler.execute
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }
  }
}
