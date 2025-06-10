import sbt.Keys._
import sbt._

/** Build dependency and repository definitions. */
object Dependencies {

  val scalaParserCombinators = Def.setting(
    "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
  )

  val scalaXml = Def.setting(
    "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
  )

  val scalaCompiler = Def.setting(
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        "org.scala-lang" %% "scala3-compiler" % scalaVersion.value
      case _ =>
        "org.scala-lang" % "scala-compiler" % scalaVersion.value
    }
  )

  val scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.13.0"

  val atmosphereJersey = "org.atmosphere" % "atmosphere-jersey" % "0.8.7"
  val javaxServlet = "javax.servlet" % "servlet-api" % "2.5"
  val jaxrsApi = "jakarta.ws.rs" % "jakarta.ws.rs-api" % "2.1.6"
  val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.19.4"
  val jerseyServlet = "com.sun.jersey" % "jersey-servlet" % jerseyCore.revision
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % jerseyCore.revision
  val jerseyServer = "com.sun.jersey" % "jersey-server" % jerseyCore.revision
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % "11.0.25"
  val jettyWebapp = jettyServer.withName("jetty-webapp")
  val jettyUtil = jettyServer.withName("jetty-util")

  val jRubyComplete = "org.jruby" % "jruby-complete" % "9.4.13.0"
  val junit = "junit" % "junit" % "4.13.2"
  val karafShell = "org.apache.karaf.shell" % "org.apache.karaf.shell.console" % "4.4.7"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.3.15"
  val osgiCore = "org.osgi" % "org.osgi.core" % "6.0.0"
  val rhinoCoffeeScript = "tv.cntt" % "rhinocoffeescript" % "1.12.7"
  val scalamd = ("org.scalatra.scalate" %% "scalamd" % "1.8.0")
  val scalaTest = Def.setting {
    Seq(
      "org.scalatest" %% "scalatest-funsuite" % "3.2.19",
      "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.19",
      "org.scalatestplus" %% "junit-4-13" % "3.2.19.1",
    )
  }
  val seleniumDriver = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "2.0.17"
  val springMVC = "org.springframework" % "spring-webmvc" % "5.3.39"
  val scalaReflect: (String, String) => ModuleID = _ % "scala-reflect" % _
  val snakeYaml = "org.yaml" % "snakeyaml" % "2.4"
  val wikitextConfluence = "org.fusesource.wikitext" % "confluence-core" % "1.4"
  val wikitextTextile = wikitextConfluence.withName("textile-core")

  val json4s = "org.json4s" %% "json4s-native" % "4.0.7"
}
