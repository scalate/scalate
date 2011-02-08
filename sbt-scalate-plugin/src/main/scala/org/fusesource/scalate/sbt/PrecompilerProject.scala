package org.fusesource.scalate.sbt

import _root_.sbt._
import java.io.File
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

trait PrecompilerProject extends ScalateProject {
  def precompilerGeneratedSourcesPath: Path = outputPath / "generated-sources" / "scalate"
  def precompilerTemplates: List[String] = Nil
  def precompilerContextClass: Option[String] = None

  lazy val precompile = task {
    withContextClassLoader(scalateClassLoader) { classLoader =>
      Thread.currentThread.setContextClassLoader(scalateClassLoader)

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
      precompiler.classesDirectory = mainCompilePath.asFile
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
}