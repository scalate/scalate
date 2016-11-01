import sbt._

/** Build dependency and repository definitions. */
object Dependencies extends Plugin {

  val commonRepositories = Seq(
    "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases",
    // Scalate Tool + JSC Converter depend on custom Fuse Karaf version
    "FuseSource Maven" at "http://repo.fusesource.com/nexus/content/groups/public/"
  )

  val atmosphereJersey = "org.atmosphere" % "atmosphere-jersey" % "0.8.0-RC1"
  val axis = "axis" % "axis" % "1.4"
  val axisWsdl = "axis" % "axis-wsdl4j" % "1.5.1"
  val camelScala214 = "org.apache.camel" % "camel-scala" % "2.14.0" // Supports Scala 2.11
  val camelSpring214 = camelScala214.copy(name = "camel-spring")
  val camelScala213 = camelScala214.copy(revision = "2.13.3") // Supports Scala 2.10
  val camelSpring213 = camelScala213.copy(name = "camel-spring")
  val confluenceSoap = "org.swift.common" % "confluence-soap" % "4.0.0" from
    "https://bitbucket.org/bob_swift/confluence-soap/downloads/confluence-soap-4.0.0.jar"
  val javaxServlet = "javax.servlet" % "servlet-api" % "2.5"
  val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.9"
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % jerseyCore.revision
  val jerseyServer = "com.sun.jersey" % "jersey-server" % jerseyCore.revision
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % "7.2.1.v20101111"
  val jettyWebapp = jettyServer.copy(name = "jetty-webapp")
  val jettyUtil = jettyServer.copy(name = "jetty-util")
  val jRebelSDK = "org.zeroturnaround" % "jr-sdk" % "4.6.2" from
    "https://repos.zeroturnaround.com/nexus/content/groups/zt-public/org/zeroturnaround/jr-sdk/4.6.2/jr-sdk-4.6.2.jar"

  val jRubyComplete = "org.jruby" % "jruby-complete" % "1.6.7.2"
  val jTidy = "net.sf.jtidy" % "jtidy" % "r938"
  val junit = "junit" % "junit" % "4.8.2"
  val karafShell = "org.apache.karaf.shell" % "org.apache.karaf.shell.console" % "2.2.0-fuse-00-43"
  val lessCssEngine = "com.asual.lesscss" % "lesscss-engine" % "1.3.3"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "0.9.26"
  val markdownJ = "org.markdownj" % "markdownj" % "0.3.0-1.0.2b4"
  val osgiCore = "org.osgi" % "org.osgi.core" % "4.2.0"
  val pegdown = "org.pegdown" % "pegdown" % "1.1.0"
  val rhinoCoffeeScript = "tv.cntt" % "rhinocoffeescript" % "1.6.2"
  val scalamd = "org.scalatra.scalate" %% "scalamd" % "1.7.0-RC1"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.0"
  val seleniumDriver = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.0a5"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.6"
  val springMVC = "org.springframework" % "spring-webmvc" % "3.0.6.RELEASE"
  val scalaParserCombinators = (org: String) => s"$org.modules" %% "scala-parser-combinators" % "1.0.4"
  val scalaXml = (org: String) => s"$org.modules" %% "scala-xml" % "1.0.5"
  val scalaCompiler: (String, String) => ModuleID = _ % "scala-compiler" % _
  val scalaReflect: (String, String) => ModuleID = _ % "scala-reflect" % _
  val snakeYaml = "org.yaml" % "snakeyaml" % "1.7"
  val wikitextConfluence = "org.fusesource.wikitext" % "confluence-core" % "1.4"
  val wikitextTextile = wikitextConfluence.copy(name = "textile-core")

}
