import sbt._
import Keys._
import com.typesafe.tools.mima.core.ProblemFilters
import com.typesafe.tools.mima.core.IncompatibleSignatureProblem
import com.typesafe.tools.mima.core.ReversedMissingMethodProblem
import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
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

  val previousVersions = Set("1.10.1")

  val mimaSettings = Seq(
    ThisBuild / mimaReportSignatureProblems := true,
    ThisBuild / mimaFailOnNoPrevious := false,
    mimaPreviousArtifacts := {
      previousVersions.map { organization.value % s"${name.value}_${scalaBinaryVersion.value}" % _ }
    },
    (Test / test) := {
      mimaReportBinaryIssues.value
      (Test / test).value
    }
  )
}
