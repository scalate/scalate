import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info)
{
  val webbytest = "org.fusesource" % "webbytest" % "1.0-SNAPSHOT"
  
  //val scripted = "org.scala-tools.sbt" % "test" % "0.6.12"

  //val sbt     = "sbt" % "sbt" % "sbt_2.7.2-0.5.6"
  //val sbtTest = "org.scala-tools.sbt" % "test" % "0.5.6"

}