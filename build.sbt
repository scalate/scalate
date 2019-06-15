import Dependencies._
import ScalateBuild._
import MimaSettings.mimaSettings
import com.typesafe.tools.mima.core._

// -----------------------------------------------------------------------------------
// README:
// Scalate project guarantees bin-compatibities for only core, util
// -----------------------------------------------------------------------------------

name := "scalate"
organization := "org.scalatra.scalate"
version := "1.9.4"
scalaVersion := crossScalaVersions.value.head
//scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.13.0", "2.12.8", "2.11.12")
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
  .published
  .settings(
    mimaSettings,
    libraryDependencies ++= Seq(
      junit % Test,
      logbackClassic % Test,
      slf4jApi,
      s"${scalaOrganization.value}.modules" %% "scala-parser-combinators" %
        (if (scalaVersion.value.startsWith("2.11")) "1.1.1" else "1.1.2"),
      s"${scalaOrganization.value}.modules" %% "scala-xml" % "1.2.0",
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    parallelExecution in Test := false,
    unmanagedSourceDirectories in Test += (sourceDirectory in Test).value / s"scala_${scalaBinaryVersion.value}")

lazy val scalateCore = scalateProject("core")
  .scalateSettings
  .published
  .settings(
    mimaSettings,
    // Somehow, MiMa in scalate-core project fails to recognize the classes that came from scalate-util project
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.IOUtil$InvalidDirectiveException"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Files"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.URLResource$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassLoaders"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassPathBuilder$AntLikeClassLoader$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Resource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.IOUtil$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.FileResourceLoader$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapStratum"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceCodeHelper"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassLoaders$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassPathBuilder"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.JavaInterops"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Lazy"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceResource$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapInstaller$Writer"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.WriteableResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMap$SmapParser$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Measurements$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ResourceNotFoundException$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.UnitOfMeasure"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Logging"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Log$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.UriResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Objects$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMap$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Strings$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.UriResource$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ResourceLoader"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.XmlHelper"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.TextResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Measurements"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.FileResource$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapStratum$LineInfo$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Threads"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ProductReflector"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.XmlHelper$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapInstaller$Writer$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapInstaller$Reader$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Objects"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassFinder"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassFinder$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Constraints$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMap"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ResourceLoader$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.StringResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Threads$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceCodeHelper$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ObjectPool"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.URIs$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ProductReflector$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.DelegateResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.UnitOfMeasure$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapStratum$LineInfo"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapInstaller"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.JavaInterops$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.URIs"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.FileResourceLoader"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Resource$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Constraints"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ClassPathBuilder$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.URLResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Files$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.StringResource$"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.ResourceNotFoundException"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.IOUtil"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapInstaller$Reader"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Strings"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.Log"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.FileResource"),
      ProblemFilters.exclude[MissingClassProblem]("org.fusesource.scalate.util.SourceMapInstaller$")
    ),
    resolvers += "sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    libraryDependencies ++= Seq(
      javaxServlet % Optional,
      logbackClassic % "runtime,optional",
      osgiCore % "provided,optional",
      rhinoCoffeeScript % Optional,
      scalamd % Optional,
      junit % Test
    ),
    libraryDependencies ++= scalaTest.value.map(_ % Test),
    libraryDependencies += scalaCompiler(scalaOrganization.value, scalaVersion.value),
    unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / s"scala_${scalaBinaryVersion.value}")
  .dependsOn(scalateUtil)

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

// TODO: Fail to compile with Scala 2.13.0-RC1
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
    description := "Guice integration for a Jersey based Scalate web application."
  )

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
