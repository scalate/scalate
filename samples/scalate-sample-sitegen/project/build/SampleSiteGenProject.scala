package org.fusesource.scalate.samples

import sbt._

class SampleSiteGenProject(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenProject {
  val scalate = "org.fusesource.scalate" % "scalate-core" % "1.4.0-SNAPSHOT" % "compile"
}
