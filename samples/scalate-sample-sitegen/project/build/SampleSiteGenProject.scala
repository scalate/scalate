package org.fusesource.scalate.samples

import _root_.sbt._
import org.fusesource.scalate.sbt._

class SampleSiteGenProject(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenProject {

  lazy val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  lazy val scalateVersion = property[String]  
  lazy val scalate = "org.fusesource.scalate" % "scalate-core" % scalateVersion.get.get
  lazy val scalate_wikitext = "org.fusesource.scalate" % "scalate-wikitext" % scalateVersion.get.get
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % "1.6.1"

  // to get jetty-run working in sbt
  lazy val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.RC0" % "test"

}
