import sbt.Keys._
import sbt._

/** Build dependency and repository definitions. */
object Dependencies {

  val atmosphereJersey = "org.atmosphere" % "atmosphere-jersey" % "0.8.7"
  val camelScala = "org.apache.camel" % "camel-scala" % "2.22.0"
  val camelSpring = camelScala.withName("camel-spring")
  val javaxServlet = "javax.servlet" % "servlet-api" % "2.5"
  val jaxbApi = "javax.xml.bind" % "jaxb-api" % "2.3.0"
  val jaxrsApi = "org.jboss.spec.javax.ws.rs" % "jboss-jaxrs-api_1.1_spec" % "1.0.1.Final"
  val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.19.4"
  val jerseyServlet = "com.sun.jersey" % "jersey-servlet" % jerseyCore.revision
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % jerseyCore.revision
  val jerseyServer = "com.sun.jersey" % "jersey-server" % jerseyCore.revision
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % "9.4.11.v20180605"
  val jettyWebapp = jettyServer.withName("jetty-webapp")
  val jettyUtil = jettyServer.withName("jetty-util")
  val jRebelSDK = "org.zeroturnaround" % "jr-sdk" % "4.6.2" from
    "https://repos.zeroturnaround.com/nexus/content/groups/zt-public/org/zeroturnaround/jr-sdk/4.6.2/jr-sdk-4.6.2.jar"

  val jRubyComplete = "org.jruby" % "jruby-complete" % "9.2.0.0"
  val junit = "junit" % "junit" % "4.12"
  val karafShell = "org.apache.karaf.shell" % "org.apache.karaf.shell.console" % "4.2.0"
  // TODO: upgrade to 1.5.x
  val lessCssEngine = "com.asual.lesscss" % "lesscss-engine" % "1.4.2"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val markdownJ = "org.markdownj" % "markdownj" % "0.3.0-1.0.2b4"
  val osgiCore = "org.osgi" % "org.osgi.core" % "6.0.0"
  val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
  val rhinoCoffeeScript = "tv.cntt" % "rhinocoffeescript" % "1.10.0"
  val scalamd = "org.scalatra.scalate" %% "scalamd" % "1.7.1"
  val scalaTest = Def.setting {
    if (scalaVersion.value == "2.13.0-M4")
      Seq("org.scalatest" %% "scalatest" % "3.0.6-SNAP1")
    else
      Seq("org.scalatest" %% "scalatest" % "3.0.5")
  }
  val seleniumDriver = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.52.0"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  val sprayJson = "io.spray" %% "spray-json" % "1.3.4"
  val springMVC = "org.springframework" % "spring-webmvc" % "5.0.7.RELEASE"
  val scalaParserCombinators = (org: String) => s"$org.modules" %% "scala-parser-combinators" % "1.1.1"
  val scalaXml = (org: String) => s"$org.modules" %% "scala-xml" % "1.1.0"
  val scalaCompiler: (String, String) => ModuleID = _ % "scala-compiler" % _
  val scalaReflect: (String, String) => ModuleID = _ % "scala-reflect" % _
  val snakeYaml = "org.yaml" % "snakeyaml" % "1.21"
  val wikitextConfluence = "org.fusesource.wikitext" % "confluence-core" % "1.4"
  val wikitextTextile = wikitextConfluence.withName("textile-core")

}
