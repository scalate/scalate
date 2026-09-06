addSbtPlugin("com.eed3si9n" % "sbt-salad-days" % "0.2.0")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.5.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.2")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.6.1")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:implicitConversions"
)
