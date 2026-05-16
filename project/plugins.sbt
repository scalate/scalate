addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.6.1")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
