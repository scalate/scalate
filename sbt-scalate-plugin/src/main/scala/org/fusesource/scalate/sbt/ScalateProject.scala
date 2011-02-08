package org.fusesource.scalate.sbt

import _root_.sbt._

import java.io.File
import java.net.{URL, URLClassLoader}
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

trait ScalateProject {
  this: DefaultWebProject =>

  def sitegenOutputPath: Path = outputPath / "sitegen"
  def sitegenTemplateProperties: Map[String, String] = Map.empty
  def scalateBootClassName: Option[String] = None

  lazy val sitegen = task {
    Thread.currentThread.setContextClassLoader(scalateClassLoader)

    // Structural Typing FTW (avoids us doing manual reflection)
    type SiteGenerator = {
      var scalateWorkDir: File
      var warSourceDirectory: File
      var resourcesSourceDirectory: File
      var targetDirectory: File
      var templateProperties: ju.Map[String,String]
      var bootClassName:String
      var info: {def apply(v1:String):Unit}
      def execute():Unit
    }

    val className = "org.fusesource.scalate.support.SiteGenerator"
    val generator = Thread.currentThread.getContextClassLoader.loadClass(className).newInstance.asInstanceOf[SiteGenerator]

    generator.info = (value:String)=>log.info(value)
    generator.scalateWorkDir = temporaryWarPath.asFile
    generator.warSourceDirectory = webappPath.asFile
    generator.resourcesSourceDirectory = mainResourcesPath.asFile
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

  def precompilerGeneratedSourcesPath: Path = outputPath / "generated-sources" / "scalate"
  def precompilerTemplates: List[String] = Nil
  def precompilerContextClass: Option[String] = None

  lazy val precompile = task {
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
    val precompiler = Thread.currentThread.getContextClassLoader.loadClass(className).newInstance.asInstanceOf[Precompiler]

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

  protected def scalateClassLoader: ClassLoader = {
    val sitegenPath = buildScalaInstance.jars.foldLeft(runClasspath) { 
       (cp, jar) => cp +++ Path.fromFile(jar)
    }
    ClasspathUtilities.toLoader(sitegenPath)
  }
}
