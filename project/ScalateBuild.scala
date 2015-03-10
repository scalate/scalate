import com.typesafe.sbt.osgi.{SbtOsgi, OsgiKeys}
import xerial.sbt.Sonatype
import Sonatype.SonatypeKeys
import sbt._
import Keys._


object ScalateBuild extends Plugin {

  implicit final class ScalateProjectSyntax(val u: Project) extends AnyVal {

    def scalateBaseSettings = u.settings(projectOpts: _*)

    def scalateSettings = scalateBaseSettings.settings(compileOpts ++ updateOpts ++ testOpts: _*)

    def osgiSettings = u.enablePlugins(SbtOsgi).settings(osgiOpts: _*)

    def dependsOn(deps: ModuleID*) = u.settings(libraryDependencies ++= deps)

    def published = u.settings(publishOpts: _*)

  }

  def scalateProject(id: String, base: Option[File] = None): Project =
    Project(s"scalate-$id", base.getOrElse(file(s"scalate-$id")))


  private def projectOpts = Seq(
    name <<= name(_.split("-|(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])").map(_.capitalize).mkString(" ")),
    version <<= version in LocalRootProject,
    organization <<= organization in LocalRootProject,
    licenses <<= licenses in LocalRootProject,
    scmInfo <<= scmInfo in LocalRootProject,
    startYear <<= startYear in LocalRootProject,
    homepage <<= homepage in LocalRootProject
  )

  private def compileOpts = Seq(
    scalaVersion <<= scalaVersion in LocalRootProject,
    crossScalaVersions <<= crossScalaVersions in LocalRootProject,
    scalacOptions in (Compile, compile) ++= Seq(Opts.compile.deprecation, Opts.compile.unchecked, "-feature", "-Xlint"),
    scalacOptions in (Test, compile) ++= Seq(Opts.compile.deprecation)
  )

  private def testOpts = Seq(
    fork in Test := true,
    baseDirectory in Test <<= baseDirectory
  )

  private def updateOpts = Seq(
//  updateOptions ~= (_.withCachedResolution(cachedResoluton = true))
  )

  private def publishOpts = Sonatype.sonatypeSettings ++ Seq(
    SonatypeKeys.profileName := "org.scalatra.scalate",
    pomExtra := developers


  )

  //private def docOpts = Seq() //TODO


  def addScalaModules(scalaMajor: Int, modules: (String => ModuleID)*) = libraryDependencies := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= scalaMajor => libraryDependencies.value ++ modules.map(_(scalaOrganization.value))
      case _ => libraryDependencies.value
    }
  }

  def addScalaDependentDeps(modules: (Int, ModuleID)*) = libraryDependencies := {
    val sv = CrossVersion.partialVersion(scalaVersion.value).map(_._2).get
    libraryDependencies.value ++ modules.filter(_._1 == sv).map(_._2)
  }

  def osgiOpts = Seq(
    packagedArtifact in(Compile, packageBin) <<= (artifact in(Compile, packageBin), OsgiKeys.bundle).identityMap,
    OsgiKeys.bundleSymbolicName <<= (organization, normalizedName) { (o, n) => s"$o.${n.stripPrefix("scalate-")}"},
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate." + normalizedName.value.stripPrefix("scalate-")),
    OsgiKeys.importPackage := Seq("scala*;version=\"%s\"".format(osgiVersionRange(scalaVersion.value)), "*"),
    OsgiKeys.exportPackage <<= OsgiKeys.privatePackage(pp => {
      val p = if (!pp.head.endsWith("*")) pp.head else pp.head.substring(0, pp.head.size - 1)
      s"!$p*.impl*" +: s"$p*" +: Nil
    }),
    OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package")
  )


  /** Create an OSGi version range for standard Scala / Typesafe versioning
    * schemes that describes binary compatible versions. Copied from Slick Build.scala. */
  def osgiVersionRange(version: String, requireMicro: Boolean = false): String =
    if (version contains '-') "${@}" // M, RC or SNAPSHOT -> exact version
    else if (requireMicro) "$<range;[===,=+)>" // At least the same micro version
    else "${range;[==,=+)}" // Any binary compatible version


  val fuseSourceMavenRepository = "FuseSource Maven" at "http://repo.fusesource.com/nexus/content/groups/public/"

  val atmosphereJersey = "org.atmosphere" % "atmosphere-jersey" % "0.8.0-RC1"

  val axis = "axis" % "axis" % "1.4"

  val axisWsdl = "axis" % "axis-wsdl4j" % "1.5.1"

  val camelScala214 = "org.apache.camel" % "camel-scala" % "2.14.0"

  val camelSpring214 = camelScala214.copy(name = "camel-spring")

  val camelScala213 = camelScala214.copy(revision = "2.13.3")

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

  val scalamd = "org.scalatra.scalate" %% "scalamd" % "1.6.1"

  val scalaTest = "org.scalatest" %% "scalatest" % "2.1.7"

  val seleniumDriver = "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.0a5"

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.6"

  val springMVC = "org.springframework" % "spring-webmvc" % "3.0.6.RELEASE"

  val scalaParserCombinators = (org: String) => s"$org.modules" %% "scala-parser-combinators" % "1.0.2"

  val scalaXml = (org: String) => s"$org.modules" %% "scala-xml" % "1.0.2"

  val scalaCompiler: (String, String) => ModuleID = _ % "scala-compiler" % _

  val snakeYaml = "org.yaml" % "snakeyaml" % "1.7"

  val wikitextConfluence = "org.fusesource.wikitext" % "confluence-core" % "1.4"

  val wikitextTextile = wikitextConfluence.copy(name = "textile-core")

  val developers = {
    <developers>
      <developer>
        <id>chirino</id>
        <name>Hiram Chirino</name>
        <url>http://hiramchirino.com/blog/</url>
        <organization>FuseSource</organization>
        <organizationUrl>http://fusesource.com/</organizationUrl>
      </developer>
      <developer>
        <id>jstrachan</id>
        <name>James Strachan</name>
        <url>http://macstrac.blogspot.com/</url>
        <organization>FuseSource</organization>
        <organizationUrl>http://fusesource.com/</organizationUrl>
      </developer>
      <developer>
        <id>matthild</id>
        <name>Matt Hildebrand</name>
      </developer>
      <developer>
        <id>PaulSandoz</id>
        <name>Paul Sandoz</name>
        <url>http://blogs.sun.com/sandoz/</url>
        <organization>Oracle</organization>
        <organizationUrl>http://oracle.com/</organizationUrl>
      </developer>
      <developer>
        <id>sptz45</id>
        <name>Spiros Tzavellas</name>
        <url>http://www.tzavellas.com</url>
        <timezone>+2</timezone>
      </developer>
      <developer>
        <id>rossabaker</id>
        <name>Ross A. Baker</name>
        <url>http://www.rossabaker.com/</url>
        <organization>CrowdStrike</organization>
        <organizationUrl>http://www.crowdstrike.com/</organizationUrl>
      </developer>
    </developers>
  }


}


