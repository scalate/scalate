import sbt._
import org.fusesource.scalate.sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) with PrecompilerWebProject {

  lazy val fusesource_snapshot_repo = "FuseSource Snapshots" at
           "http://repo.fusesource.com/nexus/content/repositories/snapshots"
  lazy val java_net_repo = "Java.net Repository" at
           "http://download.java.net/maven/2"

  lazy val scalate_core     = "org.fusesource.scalate" % "scalate-core"      % "${project.version}" 
  lazy val servlet          = "javax.servlet"          % "servlet-api"       % "${servlet-api-version}" 
  lazy val jersey           = "com.sun.jersey"         % "jersey-server"     % "${jersey-version}" 
  lazy val logback          = "ch.qos.logback"         % "logback-classic"   % "${logback-version}"

  // to get jetty-run working in sbt
  lazy val jetty_webapp     = "org.eclipse.jetty"      % "jetty-webapp"     % "7.0.2.RC0" % "test"

}
