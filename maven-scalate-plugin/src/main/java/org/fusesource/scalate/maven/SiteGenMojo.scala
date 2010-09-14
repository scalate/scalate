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
import org.apache.maven.project.MavenProject
import org.scala_tools.maven.mojo.annotations._

import org.fusesource.scalate.{DefaultRenderContext, TemplateSource, Binding, TemplateEngine}
import org.fusesource.scalate.servlet.ServletTemplateEngine
import org.fusesource.scalate.support.FileResourceLoader
import org.fusesource.scalate.wikitext.Links._
import org.fusesource.scalate.util.{Files, ClassLoaders, IOUtil}
import IOUtil._

import java.{util => ju}
import java.io.{PrintWriter, File}
import java.net.{URLClassLoader, URL}

import scala.collection.JavaConversions._

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

  @parameter
  @description("The test project classpath elements.")
  @expression("${project.testClasspathElements}")
  var testClassPathElements: ju.List[_] = _

  var engine: DummyTemplateEngine = _
  var defaultTemplateExtensions: Set[String] = Set("conf", "md", "markdown", "textile")

  def execute() = {
    targetDirectory.mkdirs();

    getLog.info("Generating static website from Scalate Templates and wiki files...");

    getLog.debug("targetDirectory: " + targetDirectory)
    getLog.debug("warSourceDirectory: " + warSourceDirectory)
    getLog.debug("resourcesSourceDirectory: " + resourcesSourceDirectory)

    engine = new DummyTemplateEngine(List(resourcesSourceDirectory, warSourceDirectory))
    val extensions = engine.extensions ++ defaultTemplateExtensions

    val urls: Array[URL] = testClassPathElements.map {
      d =>
        new File(d.toString).toURI.toURL
    }.toArray

    getLog.debug("Found project class loader URLs: " + urls.toList)

    val projectClassLoader = new URLClassLoader(urls, Thread.currentThread.getContextClassLoader)

    // lets invoke the bootstrap as we want to configure things like confluence snippet macros
    // which are outside of the ScalatePackage extension mechanism as there's no scala code gen for those filters
    ServletTemplateEngine.runBoot(List(this, engine), List(projectClassLoader, getClass.getClassLoader))

    engine.classLoader = projectClassLoader
    engine.workingDirectory = scalateWorkDir
    engine.resourceLoader = new FileResourceLoader(Some(warSourceDirectory))


    def processFile(file: File, baseuri: String, rootDir: File, copyFile: Boolean = true): Unit = {
      if (file.isDirectory()) {
        if (file.getName != "WEB-INF") {
          var children = file.listFiles();
          if (children != null) {
            for (child <- children) {
              if (child.isDirectory) {
                processFile(child, baseuri + "/" + child.getName, rootDir, copyFile)
              }
              else {
                processFile(child, baseuri, rootDir, copyFile)
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

            ClassLoaders.withContextClassLoader(projectClassLoader) {
              val html = engine.layout(TemplateSource.fromFile(file, uri))
              val sourceFile = new File(targetDirectory, uri.stripPrefix("/").stripSuffix(ext) + "html")
              sourceFile.getParentFile.mkdirs
              //IOUtil.writeBinaryFile(sourceFile, transformHtml(html, uri, rootDir).getBytes("UTF-8"))
              IOUtil.writeBinaryFile(sourceFile, html.getBytes("UTF-8"))
            }
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

    def processRootDir(rootDir: File, copyFile: Boolean = true) = processFile(rootDir, "", rootDir, copyFile)

    processRootDir(resourcesSourceDirectory, false)
    processRootDir(warSourceDirectory)

    //this.project.add(targetDirectory.getCanonicalPath);

  }

}

class DummyTemplateEngine(sourceDirectories: List[File]) extends TemplateEngine(sourceDirectories) {
  override protected def createRenderContext(uri: String, out: PrintWriter) = new DummyRenderContext(uri, this, out)

  private val responseClassName = classOf[DummyResponse].getName

  bindings = List(
    Binding("context", classOf[DummyRenderContext].getName, true, isImplicit = true),
    Binding("response", responseClassName, defaultValue = Some("new " + responseClassName + "()")))

  ServletTemplateEngine.setLayoutStrategy(this)
}

class DummyRenderContext(val _requestUri: String, _engine: TemplateEngine, _out: PrintWriter) extends DefaultRenderContext(_requestUri, _engine, _out) {
  // for static website stuff we must zap the root dir typically
  def uri(name: String) = {
    // lets deal with links to / as being to /index.html
    val link = if (name == "/") "/index.html" else name
    convertAbsoluteLinks(link, requestUri)
  }
}

class DummyResponse {
  def setContentType(value: String): Unit = {}
}