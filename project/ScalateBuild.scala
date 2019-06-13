import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.osgi.{OsgiKeys, SbtOsgi}
import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbt._
import sbtbuildinfo.BuildInfoPlugin.autoImport.BuildInfoKey
import sbtbuildinfo.{BuildInfoKeys, BuildInfoPlugin}
import sbtunidoc.BaseUnidocPlugin.autoImport._
import sbtunidoc.ScalaUnidocPlugin.autoImport.ScalaUnidoc
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.SonatypeKeys
import Dependencies._

/** Build support settings and functions. */
object ScalateBuild {

  implicit final class ScalateProjectSyntax(val u: Project) extends AnyVal {

    def scalateBaseSettings = u.settings(projectOpts: _*)

    def scalateSettings = scalateBaseSettings
      .enablePlugins(BuildInfoPlugin)
      .settings(compileOpts ++ updateOpts ++ docOpts ++ buildInfoOpts ++ testOpts: _*)

    def osgiSettings = u.enablePlugins(SbtOsgi).settings(osgiOpts: _*)

    def dependsOn(deps: ModuleID*) = u.settings(libraryDependencies ++= deps)

    def published = u.settings(publishOpts: _*)

    def notPublished = u.settings(ScalateBuild.notPublished: _*)

  }

  def scalateProject(id: String, base: Option[File] = None) =
    Project(s"scalate-$id", base.getOrElse(file(s"scalate-$id")))

  def notPublished = Seq(
    publishArtifact := false,
    publish := {},
    publishLocal := {},
    PgpKeys.publishLocalSigned := {},
    PgpKeys.publishSigned := {},
    publishTo := Some(Resolver.file("file",  target.value / "m2-cache/"))
  )

  def unidocOpts(filter: ProjectReference*): Seq[Setting[_]] =
    inConfig(ScalaUnidoc)(inTask(unidoc)(docOptsBase)) ++ Seq(
    scalacOptions in ThisBuild ++= Seq("-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath),
    apiMappings in ThisBuild += (scalaInstance.value.libraryJar →
      url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")),
    unidocProjectFilter in(ScalaUnidoc, unidoc) :=
      inAnyProject -- inProjects(filter: _*))

  private def projectOpts = Seq(
    version := (version in LocalRootProject).value,
    organization := (organization in LocalRootProject).value,
    organizationName := (organizationName in LocalRootProject).value,
    organizationHomepage := (organizationHomepage in LocalRootProject).value,
    licenses := (licenses in LocalRootProject).value,
    scmInfo := (scmInfo in LocalRootProject).value,
    startYear := (startYear in LocalRootProject).value,
    homepage := (homepage in LocalRootProject).value,
  )

  private def compileOpts = Seq(
    scalaVersion := (scalaVersion in LocalRootProject).value,
    crossScalaVersions := (crossScalaVersions in LocalRootProject).value,
    unmanagedSourceDirectories in Compile += {
      val base = baseDirectory.value / "src" / "main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v >= 13 => base / s"scala-2.13+"
        case _ => base / s"scala-2.13-"
      }
    },
    scalacOptions in(Compile, compile) ++= Seq(Opts.compile.deprecation, Opts.compile.unchecked, "-feature", "-Xlint"),
    scalacOptions in(Test, compile) ++= Seq(Opts.compile.deprecation)
  )

  private def testOpts = Seq(
    // NOTE:
    // Scala 2.13.0-M5 + ScalaTest shows many noisy messages
    // "Reporter completed abruptly with an exception after receiving event" ...
    //
    // Maybe, similar to https://github.com/scalatest/scalatest/issues/556
    // According to the GitHub issue, `fork in Test := false` is a known workaround for the issue.
    // However, it doesn't work for Scalate project. If we set it, a portion of tests fail.
    fork in Test := true,

    baseDirectory in Test := baseDirectory.value
  )

  private def updateOpts = Seq(
    updateOptions ~= (_.withCachedResolution(cachedResoluton = true))
  )

  private def publishOpts = Sonatype.sonatypeSettings ++ Seq(
    publishTo := Some(
      if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
      else Opts.resolver.sonatypeStaging
    ),
    SonatypeKeys.sonatypeProfileName := "org.scalatra.scalate",
    pomExtra := developersPomExtra :+ issuesPomExtra,
    pomIncludeRepository := (_ => false),
    publish := PgpKeys.publishSigned.value,
    publishLocal := PgpKeys.publishLocalSigned.value
  )

  private def buildInfoOpts = Seq(
    BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    BuildInfoKeys.buildInfoPackage := s"org.scalatra.scalate.${normalizedName.value.stripPrefix("scalate-")}.buildinfo",
    BuildInfoKeys.buildInfoObject := "BuildInfo"
  )

  private def docOptsBase = Seq(
    autoAPIMappings := true,
    scalacOptions ++= {
      def rev = GitKeys.gitHeadCommit.value map (_ take 7)
      def branch = GitKeys.gitCurrentBranch
      def ver = version.value
      Seq(
        "-doc-source-url", s"https://github.com/scalate/scalate/blob/${rev getOrElse branch}€{FILE_PATH}.scala",
        s"-doc-version", ver,
        s"-doc-footer", s"${name.value} $ver ${rev map (s"(Rev: " + _ + ") ") getOrElse ""}Scala ${
          scalaBinaryVersion.value} API Documentation.",
        "-implicits",
        "-diagrams"
      )
    }
  )

  private def docOpts: Seq[Setting[_]] = inConfig(Compile)(inTask(doc)(docOptsBase))

  private def osgiOpts = Seq(
    packagedArtifact in(Compile, packageBin) := ((artifact in(Compile, packageBin)).value, OsgiKeys.bundle.value),
    OsgiKeys.bundleSymbolicName := s"${organization.value}.${normalizedName.value.stripPrefix("scalate-")}",
    OsgiKeys.privatePackage := Seq("org.fusesource.scalate." + normalizedName.value.stripPrefix("scalate-")),
    OsgiKeys.importPackage := Seq("scala*;version=\"%s\"".format(osgiVersionRange(scalaVersion.value)), "*"),
    OsgiKeys.exportPackage := OsgiKeys.privatePackage(pp => {
      val p = if (!pp.head.endsWith("*")) pp.head else pp.head.substring(0, pp.head.size - 1)
      s"!$p*.impl*" +: s"$p*" +: Nil
    }).value,
    OsgiKeys.additionalHeaders := Map("-removeheaders" → "Include-Resource,Private-Package")
  )

  /** Create an OSGi version range for standard Scala / Typesafe versioning
    * schemes that describes binary compatible versions. Copied from Slick Build.scala. */
  private def osgiVersionRange(version: String, requireMicro: Boolean = false): String =
    if (version contains '-') "${@}" // M, RC or SNAPSHOT -> exact version
    else if (requireMicro) "$<range;[===,=+)>" // At least the same micro version
    else "${range;[==,=+)}" // Any binary compatible version

  def developersPomExtra =
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
      <developer>
        <id>seratch</id>
        <name>Kazuhiro Sera</name>
        <url>https://github.com/seratch</url>
      </developer>
    </developers>

  def issuesPomExtra =
    <issueManagement>
      <system>github</system>
      <url>https://github.com/scalate/scalate/issues</url>
    </issueManagement>

}
