import sbt.Keys._
import sbt._

/** Build dependency and repository definitions. */
object Dependencies {

  val atmosphereJersey = "org.atmosphere" % "atmosphere-jersey" % "0.8.7"
  val javaxServlet = "javax.servlet" % "servlet-api" % "2.5"
  val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1"
  val jaxrsApi = "org.jboss.spec.javax.ws.rs" % "jboss-jaxrs-api_1.1_spec" % "1.0.1.Final"
  val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.19.4"
  val jerseyServlet = "com.sun.jersey" % "jersey-servlet" % jerseyCore.revision
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % jerseyCore.revision
  val jerseyServer = "com.sun.jersey" % "jersey-server" % jerseyCore.revision
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % "9.4.34.v20201102"
  val jettyWebapp = jettyServer.withName("jetty-webapp")
  val jettyUtil = jettyServer.withName("jetty-util")

  val jRubyComplete = "org.jruby" % "jruby-complete" % "9.2.13.0"
  val junit = "junit" % "junit" % "4.13.1"
  val karafShell = "org.apache.karaf.shell" % "org.apache.karaf.shell.console" % "4.3.0"
  // TODO: upgrade to 1.5.x
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val osgiCore = "org.osgi" % "org.osgi.core" % "6.0.0"
  val rhinoCoffeeScript = "tv.cntt" % "rhinocoffeescript" % "1.10.0"
  val scalamd = "org.scalatra.scalate" %% "scalamd" % "1.7.3"
  val scalaTest = Def.setting {
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.2",
      "org.scalatestplus" %% "junit-4-13" % "3.2.3.0",
    )
  }
  val seleniumDriver = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.30"
  val springMVC = "org.springframework" % "spring-webmvc" % "5.3.1"
  val scalaCompiler: (String, String) => ModuleID = _ % "scala-compiler" % _
  val scalaReflect: (String, String) => ModuleID = _ % "scala-reflect" % _
  val snakeYaml = "org.yaml" % "snakeyaml" % "1.27"
  val wikitextConfluence = "org.fusesource.wikitext" % "confluence-core" % "1.4"
  val wikitextTextile = wikitextConfluence.withName("textile-core")

}
