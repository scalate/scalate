import sbt._, Keys._
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaKeys.{mimaPreviousArtifacts, mimaReportBinaryIssues}

/*
 * MiMa settings of Scalate libs.
 */
object MimaSettings {

  // The `previousVersions` must be *ALL* the previous versions to be binary compatible (e.g. Set("3.0.0", "3.0.1") for "3.0.2-SNAPSHOT").
  //
  // The following bad scenario is the reason we must obey the rule:
  //
  //  - your build is toward 3.0.2 release and the `previousVersions` is "3.0.0" only
  //  - you've added new methods since 3.0.1
  //  - you're going to remove some of the methods in 3.0.2
  //  - in this case, the incompatibility won't be detected
  //

  // NOTE: will start validating bin-compatibility since 1.9
  val previousVersions = Set(0, 1, 2).map(patch => s"1.9.$patch")
  // val previousVersions = Set.empty[String]

  val mimaSettings = MimaPlugin.mimaDefaultSettings ++ Seq(
    mimaPreviousArtifacts := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor <= 12 =>
          previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
        case _ => Set.empty
      }
    },
    test in Test := {
      mimaReportBinaryIssues.value
      (test in Test).value
    }
  )
}
