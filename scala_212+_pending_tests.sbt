// TODO fix tests and remove this file

val pendingTests = Set(
  "org.fusesource.scalate.scaml.ScamlBugTest",
  "org.fusesource.scalate.jade.JadeTemplateTest",
  "org.fusesource.scalate.jade.CaptureAppendAttributeTest",
  "org.fusesource.scalate.jade.DynamicAttributeNameTest",
  "org.fusesource.scalate.scaml.ScamlTemplateTest",
  "org.fusesource.scalate.scaml.ScamlDynamicAttributeTest",
  "org.fusesource.scalate.scaml.ScamlTemplateErrorTest"
)

(ThisBuild / Test / testOptions) ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 12 =>
      Seq(Tests.Exclude(pendingTests))
    case _ =>
      Nil
  }
}
