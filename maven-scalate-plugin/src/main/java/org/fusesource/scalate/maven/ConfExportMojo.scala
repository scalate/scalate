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
 * This goal exports confluence mark-up out of a Confluence wiki and adds the files to 
 * the resource target for Scalate to use in generating the site. Its guts are 
 * copied from the ConfluenceExport command. This should be made more 
 * modular.
 *
 * @author Eric Johnson, Fintan Bolton
 */
@goal("confexport")
@phase("generate-resources")
@requiresProject
@requiresDependencyResolution("test")
class ConfExportMojo extends AbstractMojo {
  @parameter
  @expression("${project}")
  @readOnly
  @required
  var project: MavenProject = _

  @parameter
  @description("Confluence base URL")
  @expression("${scalate.url}")
  var url: String = "https://cwiki.apache.org/confluence/"

  @parameter
  @required
  @description("The confluence space key")
  @expression("${scalate.space}")
  var space: String = "XB"

  @parameter
  @description("The directory where the exported pages will land.")
  @expression("${project.build.directory}/${project.build.finalName}")
  var target: File = _
  
  @parameter
  @description("The Confluence username to access the wiki.")
  @expression("${scalate.user}")
  var user : String = _

  @parameter
  @description("The password used to access the wiki.")
  @expression("${scalate.password}")
  var password : String = _

  @parameter
  @alias("allow-spaces")
  @description("Whether to allow spaces in filenames (boolean)")
  var allow_spaces: String = "false"
  
  @parameter
  @description("The format of the downloaded pages. Possible values are: page and conf")
  var format: String = "page"

  @parameter
  @alias("target-db")
  @description("Generate a link database for DocBook.")
  var target_db: String = "false"

  @parameter
  @description("Disable the confexport goal.")
  @expression("${scalate.confexport.skip}")
  var skip: String = "false"

  @parameter
  @description("The test project classpath elements.")
  @expression("${project.testClasspathElements}")
  var testClassPathElements: ju.List[_] = _

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

      exporter.url = this.url
      exporter.space = this.space
      exporter.target = this.target
      exporter.user = this.user
      exporter.password = this.password
      exporter.allow_spaces = this.allow_spaces.toBoolean
      exporter.format = this.format
      exporter.target_db = this.target_db.toBoolean
      exporter.execute(value => getLog.info(value) )

    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }

  }

}
