package org.fusesource.scalate.samples

import _root_.sbt._
import org.fusesource.scalate.sbt._

class SampleSiteGenProject(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenProject {

  lazy val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  lazy val scalate = "org.fusesource.scalate" % "scalate-core" % "${project.version}" 
  lazy val scalate_wikitext = "org.fusesource.scalate" % "scalate-wikitext" % "${project.version}" 
  lazy val scalate_wikitext = "org.fusesource.scalate" % "scalate-page" % "${project.version}" 
  lazy val scalate_wikitext = "org.fusesource.scalamd" % "scalamd" % "${scalamd-version}" 
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % "${slf4j-version}"

  // to get jetty-run working in sbt
  lazy val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.0.2.RC0" % "test"

}
