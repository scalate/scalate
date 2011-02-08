package org.fusesource.scalate.samples

import _root_.sbt._
import org.fusesource.scalate.sbt._

class SampleSiteGenProject(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenProject {

  val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.RC0" % "test"
  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.4.0-SNAPSHOT" % "compile"
}
