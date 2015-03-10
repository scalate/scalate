lazy val plugins = (project in file("."))
  .dependsOn(sbtOsgi)

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.0.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.2")

lazy val sbtOsgi = uri("git://github.com/arashi01/sbt-osgi.git#bd07211")
