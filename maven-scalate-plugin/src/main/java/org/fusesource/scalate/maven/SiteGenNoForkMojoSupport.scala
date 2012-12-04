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


import java.{util => ju}
import java.io.File
import java.net.{URLClassLoader, URL}


/**
 * This goal functions the same as the 'sitegen' goal but does not fork the
 * build and is suitable for attaching to the build lifecycle.
 *
 * @author <a href="http://macstrac.blogspot.com">James Strachan</a>
 */
class SiteGenNoForkMojoSupport {

  def execute(mojo:SiteGenNoForkMojo) {
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
      
      // Structural Typing FTW (avoids us doing manual reflection)
      type SiteGenerator = {
        var workingDirectory: File
        var webappDirectory: File
        var targetDirectory: File
        var templateProperties: ju.Map[String,String]
        var bootClassName:String
        var info: {def apply(v1:String):Unit}
        def execute():Unit
      }

      val className = "org.fusesource.scalate.support.SiteGenerator"
      val generator = loader.loadClass(className).newInstance.asInstanceOf[SiteGenerator]

      generator.info = (value:String)=>getLog.info(value)
      generator.workingDirectory = mojo.workingDirectory
      generator.webappDirectory = mojo.webappDirectory
      generator.targetDirectory = mojo.targetDirectory
      generator.templateProperties = mojo.templateProperties
      generator.bootClassName = mojo.bootClassName
      generator.execute
      
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }

  }

}
