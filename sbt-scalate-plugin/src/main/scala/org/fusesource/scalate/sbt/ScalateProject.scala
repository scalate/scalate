package org.fusesource.scalate.sbt

import _root_.sbt._

import java.io.File
import java.net.{URL, URLClassLoader}
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

trait ScalateProject {
  this: DefaultWebProject =>

  val sitegenOutputPath: Path = outputPath / "sitegen"
  def sitegenTemplateProperties: Map[String, String] = Map.empty
  val sitegenBootClassName: Option[String] = None

  lazy val sitegen = task {
    //
    // Lets use project's classpath when we run the site gen tool
    //
    val sitegenPath = buildScalaInstance.jars.foldLeft(runClasspath) { 
       (cp, jar) => cp +++ Path.fromFile(jar)
    }

    val loader = ClasspathUtilities.toLoader(sitegenPath)
    Thread.currentThread.setContextClassLoader(loader)

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
    val generator = loader.loadClass(className).newInstance.asInstanceOf[SiteGenerator]

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
    generator.bootClassName = sitegenBootClassName.getOrElse(null)
    generator.execute()
    None
  }
}
