import sbt._
import org.fusesource.scalate.sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenWebProject {

  lazy val fusesource_snapshot_repo = "FuseSource Snapshots" at
           "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  lazy val scalate_core     = "org.fusesource.scalate" % "scalate-core"     % "${project.version}" 
  lazy val scalate_wikitext = "org.fusesource.scalate" % "scalate-wikitext" % "${project.version}" 
  lazy val scalate_page     = "org.fusesource.scalate" % "scalate-page"     % "${project.version}" 
  lazy val scalamd          = "org.fusesource.scalamd" % "scalamd"          % "${scalamd-version}" 
  lazy val slf4j            = "org.slf4j"              % "slf4j-nop"        % "${slf4j-version}"

  // to get jetty-run working in sbt
  lazy val jetty_webapp     = "org.eclipse.jetty"      % "jetty-webapp"     % "7.0.2.RC0" % "test"

}
