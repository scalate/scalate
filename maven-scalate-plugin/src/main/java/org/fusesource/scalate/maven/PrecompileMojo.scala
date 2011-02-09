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

package org.fusesource.scalate.maven

import java.io.File
import java.util.ArrayList

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.project.MavenProject
import org.scala_tools.maven.mojo.annotations._
import java.net.{URL, URLClassLoader}
import collection.JavaConversions._


/**
 * This goal builds precompiles the Scalate templates
 * as Scala source files that be included in your standard
 * build. 
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@goal("precompile")
@phase("prepare-package")
@requiresProject
@requiresDependencyResolution("test")
class PrecompileMojo extends AbstractMojo {

  @parameter
  @expression("${project}")
  @readOnly
  @required
  var project: MavenProject = _

  @parameter
  @description("The directory where the templates files are located.")
  @expression("${basedir}/src/main/webapp")
  var warSourceDirectory: File = _

  @parameter
  @description("The directory where resources are located.")
  @expression("${basedir}/src/main/resources")
  var resourcesSourceDirectory: File = _

  @parameter
  @description("The directory where the scala code will be generated into.")
  @expression("${project.build.directory}/generated-sources/scalate")
  var targetDirectory: File = _

  @parameter
  @description("The directory containing generated classes .")
  @expression("${project.build.outputDirectory}")
  var classesDirectory:File = _

  @parameter
  @description("Additional template paths to compile.")
  var templates:ArrayList[String] = new ArrayList[String]()

  @parameter
  @description("The class name of the render context.")
  var contextClass:String = _

  @parameter
  @description("The class name of the Boot class to use.")
  var bootClassName:String = _

  @parameter
  @description("The test project classpath elements.")
  @expression("${project.testClasspathElements}")
  var classPathElements: java.util.List[_] = _


  def execute() = {

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
      
      precompiler.sources = Array(this.warSourceDirectory, this.resourcesSourceDirectory)
      precompiler.workingDirectory = this.targetDirectory
      precompiler.targetDirectory = this.classesDirectory      
      precompiler.templates = this.templates.toList.toArray
      precompiler.contextClass = this.contextClass
      precompiler.bootClassName = this.bootClassName
      precompiler.execute
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }
  }
}
