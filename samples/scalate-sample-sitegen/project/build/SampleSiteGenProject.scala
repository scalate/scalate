package org.fusesource.scalate.samples

import _root_.sbt._
import org.fusesource.scalate.sbt._

class SampleSiteGenProject(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenProject {

  val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.4.0-SNAPSHOT"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" 
  val slf4j = "org.slf4j" % "slf4j-nop" % "1.6.1"

  val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.RC0" % "test"

  //
  // We don't use a standard directory layout
  //
  override def mainScalaSourcePath = "ext"
  override def mainResourcesPath = "resources"
  override def webappPath = "src"
    
}
