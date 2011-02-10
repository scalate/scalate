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
package org.fusesource.scalate.sbt

import _root_.sbt._
import java.io.File
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

/**
 * Precompiles the templates as part of the package action.  For a web project,
 * please instead mix in {{{org.fusesource.scalate.sbt.PrecompilerWebProject}}}.
 */
trait PrecompilerProject extends ScalateProject {
  def precompilerCompilePath: Path = mainCompilePath
  def precompilerGeneratedSourcesPath: Path = outputPath / "generated-sources" / "scalate"
  def precompilerTemplates: PathFinder = Path.emptyPathFinder
  def precompilerContextClass: Option[String] = None

  lazy val precompileTemplates = precompileTemplatesAction

  def precompileTemplatesAction = precompileTemplatesTask() describedAs("Precompiles the Scalate templates")

  def precompileTemplatesTask() = task {
    withScalateClassLoader { classLoader =>

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

      val className = "org.fusesource.scalate.support.Precompiler"
      val precompiler = classLoader.loadClass(className).newInstance.asInstanceOf[Precompiler]

      precompiler.info = (value:String)=>log.info(value)
      precompiler.sources = scalateSources.get.toArray map { p: Path => p.asFile }
      precompiler.workingDirectory = precompilerGeneratedSourcesPath.asFile
      precompiler.targetDirectory = precompilerCompilePath.asFile
      precompiler.templates = precompilerTemplates.get.toArray map { p: Path => p.absolutePath }
      precompiler.contextClass = precompilerContextClass.getOrElse(null)
      precompiler.bootClassName = scalateBootClassName.getOrElse(null)
      precompiler.execute()
      None
    }
  } named ("precompile-templates")

  override def packageAction = super.packageAction dependsOn precompileTemplates
}

/**
 * Supports precompilation of templates in a web project.
 */
trait PrecompilerWebProject extends PrecompilerProject with ScalateWebProject {
  override def precompilerCompilePath: Path = temporaryWarPath / "WEB-INF" / "classes"
}

