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

import collection.JavaConversions._

import java.{util => ju}
import java.io.File
import java.net.{URLClassLoader, URL}

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.project.MavenProject

import org.scala_tools.maven.mojo.annotations._

/**
 * This goal generates static HTML files for your website using the Scalate templates, filters and wiki markups
 * you are using.  It binds to the verify phase, so it may fork a separate lifecycle in the Maven build.
 *
 * @author <a href="http://macstrac.blogspot.com">James Strachan</a>
 */
@goal("sitegen")
@phase("verify")
@executeGoal("sitegen")
@executePhase("verify")
@requiresProject
@requiresDependencyResolution("test")
class SiteGenMojo extends SiteGenNoForkMojo

/**
 * This goal functions the same as the 'sitegen' goal but does not fork the build and is suitable for attaching to the build lifecycle.
 *
 * @author <a href="http://macstrac.blogspot.com">James Strachan</a>
 */
@goal("sitegen-no-fork")
@phase("verify")
@requiresProject
@requiresDependencyResolution("test")
class SiteGenNoForkMojo extends AbstractMojo {
  @parameter
  @expression("${project}")
  @readOnly
  @required
  var project: MavenProject = _

  @parameter
  @description("The directory Scalate will use to compile templates.")
  @expression("${project.build.directory}/sitegen-workdir")
  var workingDirectory: File = _

  @parameter
  @description("The directory where the webapp is built.")
  @expression("${project.build.directory}/${project.build.finalName}")
  var webappDirectory: File = _

  @parameter
  @description("The directory where the website will be generated into.")
  @expression("${project.build.directory}/sitegen")
  var targetDirectory: File = _

  @parameter
  @description("Disable the sitegen goal.")
  @expression("${scalate.sitegen.skip}")
  var skip: String = "false"

  @parameter
  @description("The test project classpath elements.")
  @expression("${project.testClasspathElements}")
  var testClassPathElements: ju.List[_] = _

  @parameter
  @description("Properties to pass into the templates.")
  var templateProperties: ju.Map[String,String] = _

  @parameter
  @description("The class name of the Boot class to use.")
  var bootClassName:String = _

  def execute() {

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
      generator.workingDirectory = this.workingDirectory
      generator.webappDirectory = this.webappDirectory
      generator.targetDirectory = this.targetDirectory
      generator.templateProperties = this.templateProperties
      generator.bootClassName = this.bootClassName
      generator.execute
      
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }

  }

}
