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

package org.fusesource.scalate.support

import collection.JavaConversions._

import java.{util => ju}
import java.io.{PrintWriter, File}

import org.fusesource.scalate.servlet.ServletTemplateEngine
import org.fusesource.scalate._
import org.fusesource.scalate.util._

object SiteGenerator extends Log
import SiteGenerator._

/**
 * This class generates static HTML files for your website using the Scalate templates, filters and wiki markups
 * you are using.
 *
 * @author <a href="http://macstrac.blogspot.com">James Strachan</a>
 */
class SiteGenerator {
  var scalateWorkDir: File = _
  var warSourceDirectory: File = _
  var resourcesSourceDirectory: File = _
  var targetDirectory: File = _
  var templateProperties: ju.Map[String,String] = _
  var bootClassName:String = _
  var info:{def apply(v1:String):Unit} = (value:String)=>println(value)
  

  def execute() = {
    targetDirectory.mkdirs();

    info("Generating static website from Scalate Templates and wiki files...");
    info("template properties: " + templateProperties)

    var engine = new DummyTemplateEngine(List(resourcesSourceDirectory, warSourceDirectory))
    engine.classLoader = Thread.currentThread.getContextClassLoader
    engine.workingDirectory = scalateWorkDir
    engine.resourceLoader = new FileResourceLoader(Some(warSourceDirectory))

    if( bootClassName!=null ) {
      engine.bootClassName = bootClassName
    }
    engine.boot

    val attributes: Map[String,Any] = if (templateProperties != null) {
      templateProperties.toMap
    } else {
      Map()
    }


    def processFile(file: File, baseuri: String, rootDir: File, copyFile: Boolean = true): Unit = {
      if (file.isDirectory()) {
        if (file.getName != "WEB-INF" && !file.getName.startsWith("_") ) {
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
        if (parts.size > 1 && !file.getName.startsWith("_")) {
          val uri = baseuri + "/" + file.getName()
          // uri = uri.replace(':', '_')
          val ext = parts.last
          if (engine.extensions.contains(ext)) {

            try {
              val source = TemplateSource.fromFile(file, uri)
              val html = engine.layout(source, attributes)
              val sourceFile = new File(targetDirectory, appendHtmlPostfix(uri.stripPrefix("/")))

              info("    processing " + file + " with uri: " + uri + " => ")
              sourceFile.getParentFile.mkdirs
              //IOUtil.writeBinaryFile(sourceFile, transformHtml(html, uri, rootDir).getBytes("UTF-8"))
              IOUtil.writeBinaryFile(sourceFile, html.getBytes("UTF-8"))
            }
            catch {
              case e: NoValueSetException => info(e.getMessage + ". Ignored template file due to missing attributes: " + file.getCanonicalPath)
              case e => throw new Exception(e.getMessage + ". When processing file: " + file.getCanonicalPath, e)
            }
          } else {
            debug("    ignoring " + file + " with uri: " + uri + " extension: " + ext + " not in " + engine.extensions)

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

  protected var validFileExcentions = Set("js", "css", "rss", "atom", "htm", "xml", "csv", "json")

  protected def appendHtmlPostfix(uri: String): String = {
    val answer = Files.dropExtension(uri)
    val ext = Files.extension(answer)
    if (validFileExcentions.contains(ext)) {
      answer
    } else {
      answer + ".html"
    }
  }

}

class DummyTemplateEngine(sourceDirectories: List[File]) extends TemplateEngine(sourceDirectories) {
  override protected def createRenderContext(uri: String, out: PrintWriter) = new DummyRenderContext(uri, this, out)

  private val responseClassName = "_root_."+classOf[DummyResponse].getName

  bindings = List(
    Binding("context", "_root_."+classOf[DummyRenderContext].getName, true, isImplicit = true),
    Binding("response", responseClassName, defaultValue = Some("new " + responseClassName + "()")))

  ServletTemplateEngine.setLayoutStrategy(this)
}

class DummyRenderContext(val _requestUri: String, _engine: TemplateEngine, _out: PrintWriter) extends DefaultRenderContext(_requestUri, _engine, _out) {
  // for static website stuff we must zap the root dir typically
  override def uri(name: String) = {
    // lets deal with links to / as being to /index.html
    val link = if (name == "/") "/index.html" else name
    Links.convertAbsoluteLinks(link, requestUri)
  }
}

class DummyResponse {
  def setContentType(value: String): Unit = {}
}