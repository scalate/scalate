import sbt._

class ScalateProject(info: ProjectInfo) extends DefaultWebProject(info) {

  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

  override def testOptions = {
    super.testOptions ++ 
    Seq(TestArgument("-Dbasedir=" + ".".absolutePath))
  }
}
