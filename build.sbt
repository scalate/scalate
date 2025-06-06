import Dependencies._
import ScalateBuild._
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.core._

// -----------------------------------------------------------------------------------
// README:
// Scalate project guarantees bin-compatibities for only core, util
// -----------------------------------------------------------------------------------

def Scala212 = "2.12.20"
def Scala213 = "2.13.16"
def Scala3 = "3.3.6"

addCommandAlias("SetScala212", s"++ ${Scala212}!")
addCommandAlias("SetScala213", s"++ ${Scala213}!")
addCommandAlias("SetScala3", s"++ ${Scala3}!")

name := "scalate"
organization := "org.scalatra.scalate"
version := "1.10.1"
scalaVersion := Scala213
crossScalaVersions := Seq(Scala3, Scala213, Scala212)
javacOptions ++= Seq("-source", "1.8")
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
startYear := Some(2010)
licenses += "The Apache Software License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")
scmInfo := Some(
  ScmInfo(url("https://github.com/scalate/scalate"),
  "scm:git:git://github.com/scalate/scalate.git",
  Some("scm:git:ssh://git@github.com:scalate/scalate.git"))
)
homepage := Some(url("https://scalate.github.io/scalate"))
enablePlugins(ScalaUnidocPlugin)
unidocOpts(filter = scalateWar, scalateWeb)
notPublished

lazy val scalateUtil = scalateProject("util")
  .scalateSettings
  .published
  .settings(
    mimaSettings,
    libraryDependencies ++= Seq(
      junit % Test,
      logbackClassic % Test,
      slf4jApi,
      scalaParserCombinators.value,
      scalaXml.value,
      scalaCollectionCompat,
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    Test / parallelExecution := false,
  )
  .enablePlugins(MimaPlugin)

lazy val scalateCore = scalateProject("core")
  .scalateSettings
  .published
  .settings(
    mimaSettings,
    resolvers += "sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    libraryDependencies ++= Seq(
      javaxServlet % Optional,
      logbackClassic % "runtime,optional",
      osgiCore % "provided,optional",
      rhinoCoffeeScript % Optional,
      scalamd % Optional,
      scalaCollectionCompat,
      junit % Test,
      json4s % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    libraryDependencies += scalaCompiler.value,
    buildInfoPackage := "org.fusesource.scalate.buildinfo"
  )
  .dependsOn(scalateUtil)
  .enablePlugins(MimaPlugin)

// -----------------------------------------------------------------------------------

lazy val scalateTest = scalateProject("test")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always",
    libraryDependencies ++= Seq(
      jettyServer,
      jettyWebapp,
      jettyUtil,
      junit,
      seleniumDriver
    ), 
    libraryDependencies ++= scalaTest.value,
    description := "Scalate Test Support Classes.")
  .settings(mimaSettings)
  .enablePlugins(MimaPlugin)

// TODO: Fail to compile with Scala 2.13.0-RC1
lazy val scalateGuice = scalateProject("guice")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      atmosphereJersey % Provided,
      javaxServlet,
      jerseyCore,
      jerseyGuice,
      junit % Test,
      logbackClassic % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Guice integration for a Jersey based Scalate web application."
  )

lazy val scalateJruby = scalateProject("jruby")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      jRubyComplete
    ),
    description := "Scalate integration with JRuby including Ruby based filters such as sass.")

lazy val scalateJspConverter = scalateProject("jsp-converter")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      karafShell,
      junit % Test,
      logbackClassic % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Converter for JSP to SSP",
    buildInfoPackage := "org.fusesource.scalate.converter.buildinfo")

lazy val scalatePage = scalateProject("page")
  .scalateSettings
  .published
  .dependsOn(scalateCore, scalateWikitext, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      rhinoCoffeeScript,
      scalamd,
      snakeYaml,
      scalaCollectionCompat,
    ),
    description := "Scalate multipart page filter (similar to Webgen page format).")

lazy val scalateSpringMVC = scalateProject("spring-mvc")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      javaxServlet % Provided,
      springMVC,
      scalaCollectionCompat,
      junit % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Scalate Spring MVC integration.",
    buildInfoPackage := "org.fusesource.scalate.spring.buildinfo")

lazy val scalateWar = scalateProject("war")
  .scalateSettings
  .notPublished
  .dependsOn(scalateWeb, scalateJersey, scalateTest % Test)
  .enablePlugins(TomcatPlugin)
  .settings(
    libraryDependencies ++= Seq(
      logbackClassic,
      jerseyServer,
      jerseyCore
    ),
    description := "Scalate Base Web Application",
    Compile / packageBin / publishArtifact := false,
    Test / parallelExecution := false,
    (Test / unmanagedResourceDirectories) += baseDirectory.value / "src/main/webapp")

lazy val scalateJAXRS = scalateProject("jaxrs")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      javaxServlet % Provided,
      jaxrsApi % Provided
    ),
    description := "JAXRS integration for a Scalate web application")

lazy val scalateJersey = scalateProject("jersey")
  .scalateSettings
  .published
  .dependsOn(scalateJAXRS, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      jaxrsApi % Provided,
      javaxServlet % Provided,
      jerseyCore % Provided,
      jerseyServlet % Provided,
      jerseyServer % Provided,
      scalaCollectionCompat,
    ),
    description := "Jersey integration for a Scalate web application")

lazy val scalateWeb = scalateProject("web")
  .scalateSettings
  .published
  .dependsOn(scalatePage, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      javaxServlet % Provided
    ),
    description := "Single dependency for all modules required to use Scalate and common wiki formats.")

lazy val scalateWikitext = scalateProject("wikitext")
  .scalateSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      wikitextConfluence,
      wikitextTextile,
      scalaCollectionCompat,
      logbackClassic % Test
    ),
    description := "Scalate WikiText integration for Markdown and Confluence notations.")
