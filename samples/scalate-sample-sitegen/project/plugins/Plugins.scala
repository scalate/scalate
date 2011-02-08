package org.fusesource.scalate.samples

import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info)
{
  val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  val scalatePlugin = "org.fusesource.scalate" % "sbt-scalate-plugin" % "1.4.0-SNAPSHOT"
}