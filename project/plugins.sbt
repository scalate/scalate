addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.0.2")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.1")
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.1")
//addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.6") TODO: What is our Binary compatibility policy?
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
