package org.fusesource.scalate.sbt

import _root_.sbt._
import java.io.File
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

/**
 * Precompiles Scalate templates.  The templates will be compiled into Scala
 * sources, then built as part of the standard compile action.
 */
trait PrecompilerProject extends ScalateProject {
  def precompilerGeneratedSourcesPath: Path = outputPath / "generated-sources" / "scalate"
  def precompilerTemplates: List[String] = Nil
  def precompilerContextClass: Option[String] = None

  lazy val precompileTemplates = precompileTemplatesAction

  def precompileTemplatesAction = precompileTemplatesTask() describedAs("Precompiles the Scalate templates")

  def precompileTemplatesTask() = task {
    withScalateClassLoader { classLoader =>

      // Structural Typing FTW (avoids us doing manual reflection)
      type Precompiler = {
        var warSourceDirectory: File
        var resourcesSourceDirectory: File
        var workingDirectory: File
        var classesDirectory: File
        var templates: ju.ArrayList[String]
        var info: {def apply(v1:String):Unit}
        var contextClass: String
        var bootClassName:String
        def execute(): Unit
      }

      val className = "org.fusesource.scalate.support.Precompiler"
      val precompiler = classLoader.loadClass(className).newInstance.asInstanceOf[Precompiler]

      precompiler.info = (value:String)=>log.info(value)
      precompiler.warSourceDirectory = webappPath.asFile
      precompiler.resourcesSourceDirectory = mainResourcesPath.asFile
      precompiler.workingDirectory = precompilerGeneratedSourcesPath.asFile
      precompiler.classesDirectory = (temporaryWarPath / "WEB-INF" / "classes").asFile
      precompiler.templates = {
        val list = new ju.ArrayList[String]
        list ++ precompilerTemplates
        list
      }
      precompiler.contextClass = precompilerContextClass.getOrElse(null)
      precompiler.bootClassName = scalateBootClassName.getOrElse(null)
      precompiler.execute()
      None
    }
  }

  override def packageAction = super.packageAction dependsOn precompileTemplates
}