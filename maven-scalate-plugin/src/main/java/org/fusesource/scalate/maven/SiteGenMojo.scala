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

import org.apache.maven.plugin.AbstractMojo
import org.fusesource.scalate.{DefaultRenderContext, TemplateSource, Binding, TemplateEngine}
import java.io.{PrintWriter, File};
import org.fusesource.scalate.servlet.ServletTemplateEngine
import org.fusesource.scalate.support.FileResourceLoader
import org.fusesource.scalate.util.IOUtil

import org.apache.maven.project.MavenProject

import org.scala_tools.maven.mojo.annotations._


/**
 * This goal generates static HTML files for your website using the Scalate templates, filters and wiki markups
 * you are using
 *
 * @author <a href="http://macstrac.blogspot.com">James Strachan</a>
 */
@goal("sitegen")
@phase("verify")
@requiresProject
@requiresDependencyResolution("test")
class SiteGenMojo extends AbstractMojo {
  @parameter
  @expression("${project}")
  @readOnly
  @required
  var project: MavenProject = _

  @parameter
  @description("The directory Scalate will use to compile templates.")
  @expression("${project.build.directory}/sitegen-workdir")
  var scalateWorkDir: File = _

  @parameter
  @description("The directory where the templates files are located.")
  @expression("${basedir}/src/main/webapp")
  var warSourceDirectory: File = _

  @parameter
  @description("The directory where resources are located.")
  @expression("${basedir}/src/main/resources")
  var resourcesSourceDirectory: File = _

  @parameter
  @description("The directory where the website will be generated into.")
  @expression("${project.build.directory}/sitegen")
  var targetDirectory: File = _

  def execute() = {
    targetDirectory.mkdirs();

    getLog.info("Generating static website from Scalate Templates and wiki files...");

    getLog.debug("targetDirectory: " + targetDirectory)
    getLog.debug("warSourceDirectory: " + warSourceDirectory)
    getLog.debug("resourcesSourceDirectory: " + resourcesSourceDirectory)

    // TODO: need to customize bindings
    var engine = new DummyTemplateEngine()
    engine.workingDirectory = scalateWorkDir
    engine.resourceLoader = new FileResourceLoader(Some(warSourceDirectory))

    val extensions = engine.extensions ++ Set("conf", "md", "markdown", "textile")

    def processFile(file: File, baseuri: String, copyFile: Boolean = true): Unit = {
      if (file.isDirectory()) {
        if (file.getName != "WEB-INF") {
          var children = file.listFiles();
          if (children != null) {
            for (child <- children) {
              if (child.isDirectory) {
                processFile(child, baseuri + "/" + child.getName, copyFile)
              }
              else {
                processFile(child, baseuri, copyFile)
              }
            }
          }
        }
      } else {
        val parts = file.getName.split('.')
        if (parts.size > 1) {
          val uri = baseuri + "/" + file.getName()
          // uri = uri.replace(':', '_')
          val ext = parts.last
          if (extensions.contains(ext)) {
            getLog.info("    processing " + file + " with uri: " + uri)

            val html = engine.layout(TemplateSource.fromFile(file, uri))
            val sourceFile = new File(targetDirectory, uri.stripPrefix("/").stripSuffix(ext) + "html")
            sourceFile.getParentFile.mkdirs
            IOUtil.writeBinaryFile(sourceFile, html.getBytes("UTF-8"))
          } else {
            getLog.debug("    ignoring " + file + " with uri: " + uri + " extension: " + ext + " not in " + extensions)

            if (copyFile) {
              // lets copy the file across if its not a template
              val sourceFile = new File(targetDirectory, uri.stripPrefix("/"))
              IOUtil.copy(file, sourceFile)
            }
          }
        }
      }
    }

    processFile(resourcesSourceDirectory, "", false)
    processFile(warSourceDirectory, "")

    //this.project.add(targetDirectory.getCanonicalPath);
  }
}

class DummyTemplateEngine extends TemplateEngine {
  override protected def createRenderContext(uri: String, out: PrintWriter) = new DummyRenderContext(uri, this, out)

  private val responseClassName = classOf[DummyResponse].getName

  bindings = List(
    Binding("context", classOf[DummyRenderContext].getName, true, isImplicit = true),
    Binding("response", responseClassName, defaultValue = Some("new " + responseClassName + "()")))

  ServletTemplateEngine.setLayoutStrategy(this)
}

class DummyRenderContext(val requestURI: String, _engine: TemplateEngine, _out: PrintWriter) extends DefaultRenderContext(_engine, _out) {
  // for static website stuff we must zap the root dir typically
  def uri(name: String) = name.stripPrefix("/")
}

class DummyResponse {
  def setContentType(value: String): Unit = {}
}