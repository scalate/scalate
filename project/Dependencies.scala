import sbt.Keys._
import sbt._

/** Build dependency and repository definitions. */
object Dependencies {

  val scalaParserCombinators = Def.setting(
    if (scalaBinaryVersion.value == "2.11") {
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1"
    } else {
      "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
    }
  )

  val scalaXml = Def.setting(
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) =>
        "org.scala-lang.modules" %% "scala-xml" % "1.3.0"
      case _ =>
        "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
    }
  )

  val atmosphereJersey = "org.atmosphere" % "atmosphere-jersey" % "0.8.7"
  val javaxServlet = "javax.servlet" % "servlet-api" % "2.5"
  val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.1"
  val jaxrsApi = "org.jboss.spec.javax.ws.rs" % "jboss-jaxrs-api_1.1_spec" % "1.0.1.Final"
  val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.19.4"
  val jerseyServlet = "com.sun.jersey" % "jersey-servlet" % jerseyCore.revision
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % jerseyCore.revision
  val jerseyServer = "com.sun.jersey" % "jersey-server" % jerseyCore.revision
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % "11.0.12"
  val jettyWebapp = jettyServer.withName("jetty-webapp")
  val jettyUtil = jettyServer.withName("jetty-util")

  val jRubyComplete = "org.jruby" % "jruby-complete" % "9.3.8.0"
  val junit = "junit" % "junit" % "4.13.2"
  val karafShell = "org.apache.karaf.shell" % "org.apache.karaf.shell.console" % "4.4.1"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.3.3"
  val osgiCore = "org.osgi" % "org.osgi.core" % "6.0.0"
  val rhinoCoffeeScript = "tv.cntt" % "rhinocoffeescript" % "1.12.7"
  val scalamd = "org.scalatra.scalate" %% "scalamd" % "1.7.3"
  val scalaTest = Def.setting {
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.14",
      "org.scalatestplus" %% "junit-4-13" % "3.2.14.0",
    )
  }
  val seleniumDriver = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "2.0.3"
  val springMVC = "org.springframework" % "spring-webmvc" % "5.3.23"
  val scalaCompiler: (String, String) => ModuleID = _ % "scala-compiler" % _
  val scalaReflect: (String, String) => ModuleID = _ % "scala-reflect" % _
  val snakeYaml = "org.yaml" % "snakeyaml" % "1.33"
  val wikitextConfluence = "org.fusesource.wikitext" % "confluence-core" % "1.4"
  val wikitextTextile = wikitextConfluence.withName("textile-core")

  val json4s = "org.json4s" %% "json4s-native" % "4.0.6"
}
