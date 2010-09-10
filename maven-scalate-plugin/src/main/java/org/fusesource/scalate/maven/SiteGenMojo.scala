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
import org.fusesource.scalate.util.{Files, ClassLoaders, IOUtil}
import IOUtil._

import java.{util => ju}
import java.io.{PrintWriter, File}
import java.net.{URLClassLoader, URL}

import scala.collection.JavaConversions._

object Links {

  /**
   * Converts an absolute link rom the root directory to a relative link from the current
   * request URI
   */
  def convertAbsoluteLinks(link: String, requestURI: String): String = if (link.startsWith("/")) {
    var n = link.stripPrefix("/").split('/').toList
    var r = requestURI.stripPrefix("/").split('/').toList

    // lets strip the common prefixes off
    while (n.size > 1 && r.size > 1 && n.head == r.head) {
      n = n.tail
      r = r.tail
    }

    val prefix = "../" * (r.size - 1)
    n.mkString(prefix, "/", "")
  } else {
    link
  }
}

import Links._

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

  var engine = new DummyTemplateEngine()
  val extensions = engine.extensions ++ Set("conf", "md", "markdown", "textile")

  def execute() = {
    targetDirectory.mkdirs();

    getLog.info("Generating static website from Scalate Templates and wiki files...");

    getLog.debug("targetDirectory: " + targetDirectory)
    getLog.debug("warSourceDirectory: " + warSourceDirectory)
    getLog.debug("resourcesSourceDirectory: " + resourcesSourceDirectory)

    val urls: Array[URL] = testClassPathElements.map {
      d =>
        new File(d.toString).toURI.toURL
    }.toArray

    getLog.debug("Found project class loader URLs: " + urls.toList)

    val projectClassLoader = new URLClassLoader(urls, Thread.currentThread.getContextClassLoader)

    // lets invoke the bootstrap as we want to configure things like confluence snippet macros
    // which are outside of the ScalatePackage extension mechanism as there's no scala code gen for those filters
    ServletTemplateEngine.runBoot(List(projectClassLoader, getClass.getClassLoader))

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
              IOUtil.writeBinaryFile(sourceFile, transformHtml(html, uri, rootDir).getBytes("UTF-8"))
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

  /**
   * Lets fix up any links which are local and do notcontain a file extension
   */
  def transformHtml(html: String, uri: String, rootDir: File): String = linkRegex.replaceAllIn(html, {
    // for some reason we don't just get the captured group - no idea why. Instead we get...
    //
    //   m.matched == m.group(0) == "<a class="foo" href='linkUri'"
    //   m.group(1) == "linkUri"
    //
    // so lets replace the link URI in the matched text to just change the contents of the link
    m =>
      val link = m.group(1)
      val matched = m.matched
      matched.dropRight(link.size + 1) + transformLink(link, uri, rootDir) + matched.last
  })

  /**
   * If a link is external or includes a dot then assume its OK, otherwise append html extension
   */
  def transformLink(link: String, requestUri: String, rootDir: File) = {
    def relativeLink(link: String) = convertAbsoluteLinks(link, requestUri)

    /**
     * lets start at the root directory and keep navigating through all files until we find a file name that matches
     * the given link
     */
    def findConfluenceLink = {
      // for now we are just using non-path names but if we wanted to support relative badly named files
      // we could use: link.split('/').last
      val name1 = link.toLowerCase
      val name2 = name1.replace(' ', '-')
      val extensions = engine.extensions

      rootDir.find{ f =>
        val n = f.nameDropExtension.toLowerCase
        n == name1 || n == name2 && extensions.contains(f.extension.toLowerCase)
      } match {
        case Some(file) =>
          "/" + Files.dropExtension(Files.relativeUri(rootDir, file))
        case _ => link
      }
    }

    if (link.contains(':')) {
      // external so leave as is
      link
    } else {
      if (link.contains('.')) {
        relativeLink(link)
      }
      else {
        val newLink = if (link.contains('/')) {
          link
        } else {
          // if we have no path then assume we are a bad confluence link and try find the actual path
          findConfluenceLink
        }
        relativeLink(newLink) + ".html"
      }
    }
  }

  protected val linkRegex = "(?i)<(?>link|a|img|script)[^>]*?(?>href|src)\\s*?=\\s*?[\\\"'](.*?)[\\\"'][^>]*?".r
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
  def uri(name: String) = {
    // lets deal with links to / as being to /index.html
    val link = if (name == "/") "/index.html" else name
    convertAbsoluteLinks(link, requestURI)
  }
}

class DummyResponse {
  def setContentType(value: String): Unit = {}
}