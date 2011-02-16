/**
 *  Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.sbt

import _root_.sbt._
import java.io.File
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

/**
 * Generates static HTML files for your website using the Scalate templates.
 * It runs as a dependency of the package action.
 */
trait SiteGenWebProject extends BasicWebScalaProject with ScalateWebProject {
  /**
   * The directory into which the site will be generated.
   */
  def sitegenOutputPath: Path = outputPath / "sitegen"

  /**
   * Attributes to pass into the templates.
   */
  def sitegenTemplateProperties: Map[String, String] = Map.empty

  lazy val generateSite = generateSiteAction

  def generateSiteAction = generateSiteTask()
    .describedAs("Generates a static site.")
    .dependsOn(prepareWebapp)

  def generateSiteTask() = task {
    withScalateClassLoader { classLoader =>
      
      // Structural Typing FTW (avoids us doing manual reflection)
      type SiteGenerator = {
        var webappDirectory: File
        var workingDirectory: File
        var targetDirectory: File
        var templateProperties: ju.Map[String,String]
        var bootClassName:String
        var info: {def apply(v1:String):Unit}
        def execute():Unit
      }

      val className = "org.fusesource.scalate.support.SiteGenerator"
      val generator = classLoader.loadClass(className).newInstance.asInstanceOf[SiteGenerator]

      generator.info = (value:String)=>log.info(value)
      generator.webappDirectory = temporaryWarPath.asFile
      generator.targetDirectory = sitegenOutputPath.asFile
      generator.templateProperties = {
        val jclMap = new jcl.HashMap[String, String]
        jclMap ++= sitegenTemplateProperties
        jclMap
      }
      generator.bootClassName = scalateBootClassName.getOrElse(null)
      generator.execute()
      None
    }
  } named ("generate-site")

  //
  // We don't use a standard directory layout
  //
  override def mainScalaSourcePath = "ext"
  override def mainResourcesPath = "resources"
  override def webappPath = "src"

  override def packageAction = super.packageAction dependsOn generateSite
}
