import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  
  lazy val fusesource_snapshot_repo = "FuseSource Snapshots" at
           "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  lazy val scalate_plugin = "org.fusesource.scalate" % "sbt-scalate-plugin" % "${project.version}"

}