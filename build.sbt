import Dependencies._
import ScalateBuild._
import MimaSettings.mimaSettings

// -----------------------------------------------------------------------------------
// README:
// Scalate project guarantees bin-compatibities for only core, util
// -----------------------------------------------------------------------------------

name := "Scalate"
organization := "org.scalatra.scalate"
version := "1.9.0-RC3-SNAPSHOT"
scalaVersion := crossScalaVersions.value.head
crossScalaVersions := Seq("2.12.6", "2.11.12", "2.13.0-M4")
javacOptions ++= Seq("-source", "1.8")
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
startYear := Some(2010)
licenses += "The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
scmInfo := Some(
  ScmInfo(url("http://github.com/scalate/scalate"),
  "scm:git:git://github.com/scalate/scalate.git",
  Some("scm:git:ssh://git@github.com:scalate/scalate.git"))
)
homepage := Some(url("http://scalate.github.io/scalate"))
enablePlugins(ScalaUnidocPlugin)
unidocOpts(filter = scalateJrebel, scalateWar, scalateWeb)
notPublished

lazy val scalateUtil = scalateProject("util")
  .scalateSettings
  .osgiSettings
  .published
  .settings(
    libraryDependencies ++= Seq(
      junit % Test,
      logbackClassic % Test,
      slf4jApi
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Scalate Utilities.",
    parallelExecution in Test := false,
    addScalaModules(11, scalaXml, scalaParserCombinators),
    addScalaModules(12, scalaXml, scalaParserCombinators),
    unmanagedSourceDirectories in Test += (sourceDirectory in Test).value / s"scala_${scalaBinaryVersion.value}")
  .settings(mimaSettings)

lazy val scalateCore = scalateProject("core")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateUtil)
  .settings(
    libraryDependencies ++= Seq(
      javaxServlet % Optional,
      logbackClassic % "runtime,optional",
      osgiCore % "provided,optional",
      rhinoCoffeeScript % Optional,
      scalamd % Optional,
      junit % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Scalate Core",
    libraryDependencies += scalaCompiler(scalaOrganization.value, scalaVersion.value),
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate"),
    unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / s"scala_${scalaBinaryVersion.value}")
  .settings(mimaSettings)

// -----------------------------------------------------------------------------------

lazy val scalateTest = scalateProject("test")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
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

lazy val scalateCamel = scalateProject("camel")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    description := "Camel component for Scalate.",
    libraryDependencies ++= Seq(camelScala, camelSpring, jaxbApi)
  )

lazy val scalateGuice = scalateProject("guice")
  .scalateSettings
  .osgiSettings
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
    description := "Guice integration for a Jersey based Scalate web application.")

lazy val scalateJrebel = scalateProject("jrebel")
  .scalateSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      jRebelSDK % Provided
    ),
    description := "JRebel plugin for reloading Scalate templates on class reload.")

lazy val scalateJruby = scalateProject("jruby")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      jRubyComplete,
      logbackClassic % Test
    ),
    description := "Scalate integration with JRuby including Ruby based filters such as sass.")

lazy val scalateJspConverter = scalateProject("jsp-converter")
  .scalateSettings
  .osgiSettings
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
    resolvers ++= commonRepositories,
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate.converter"),
    buildInfoPackage := "org.fusesource.scalate.converter.buildinfo")

lazy val scalateLess = scalateProject("less")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      lessCssEngine,
      logbackClassic % Test
    ),
    description := "Scalate LESS filter.",
    OsgiKeys.bundleSymbolicName := "org.scalatra.scalate.filter.less",
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate.filter.less"))

lazy val scalateMarkdownJ = scalateProject("markdownj")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      markdownJ,
      junit % Test,
      logbackClassic % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Scalate MarkdownJ filter.",
    OsgiKeys.bundleSymbolicName := "org.scalatra.scalate.filter.markdownj",
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate.filter.markdownj"))

lazy val scalatePage = scalateProject("page")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateWikitext, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      rhinoCoffeeScript,
      scalamd,
      snakeYaml
    ),
    description := "Scalate multipart page filter (similar to Webgen page format).")

lazy val scalatePegdown = scalateProject("pegdown")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      pegdown
    ),
    description := "Scalate Pegdown filter.",
    OsgiKeys.bundleSymbolicName := "org.scalatra.scalate.filter.pegdown",
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate.filter.pegdown"))


lazy val scalateSpringMVC = scalateProject("spring-mvc")
  .scalateSettings
  .osgiSettings
  .published
  .dependsOn(scalateCore)
  .settings(
    libraryDependencies ++= Seq(
      javaxServlet % Provided,
      springMVC,
      junit % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    description := "Scalate Spring MVC integration.",
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate.spring.view"),
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
    publishArtifact in (Compile, packageBin) := false,
    parallelExecution in Test := false,
    unmanagedResourceDirectories in Test += baseDirectory.value / "src/main/webapp")

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
      jerseyServer % Provided
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
  .osgiSettings
  .published
  .dependsOn(scalateCore, scalateTest % Test)
  .settings(
    libraryDependencies ++= Seq(
      wikitextConfluence,
      wikitextTextile,
      logbackClassic % Test
    ),
    description := "Scalate WikiText integration for Markdown and Confluence notations.")
