import sbt._

class ScalateParentProject(info: ProjectInfo) extends ParentProject(info) {

  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

  // Projects
  lazy val scalate_core = project("scalate-core", "scalate-core", new ScalateCore(_))
  lazy val scalate_test = project("scalate-test", "scalate-test", new ScalateTest(_), scalate_core)
  lazy val scalate_camel = project("scalate-camel", "scalate-camel", new ScalateCamel(_), scalate_core, scalate_test)
  lazy val scalate_jsp_converter = project("scalate-jsp-converter", "scalate-jsp-converter", new ScalateJspConverter(_), scalate_core)
  lazy val scalate_war = project("scalate-war", "scalate-war", new ScalateWar(_), scalate_core, scalate_test)
  lazy val scalate_sample = project("scalate-sample", "scalate-sample", new ScalateSample(_), scalate_core, scalate_test, scalate_war)
  lazy val scalate_bookstore = project("scalate-bookstore", "scalate-bookstore", new ScalateBookstore(_), scalate_core, scalate_test, scalate_war)

  class ScalateProject(info: ProjectInfo) extends DefaultProject(info) { 

    override def testOptions = {
      super.testOptions ++ 
      Seq(TestArgument("-Dbasedir=" + ".".absolutePath))
    }
  }

  class ScalateWebProject(info: ProjectInfo) extends DefaultWebProject(info) { 

    override def testOptions = {
      super.testOptions ++ 
      Seq(TestArgument("-Dbasedir=" + ".".absolutePath))
    }
  }


  class ScalateCore(info: ProjectInfo) extends ScalateProject(info) {
  }

  class ScalateTest(info: ProjectInfo) extends ScalateProject(info) {
  }

  class ScalateCamel(info: ProjectInfo) extends ScalateProject(info) {
  }

  class ScalateJspConverter(info: ProjectInfo) extends ScalateProject(info) {
  }

  class ScalateWar(info: ProjectInfo) extends ScalateWebProject(info) {
  }

  class ScalateSample(info: ProjectInfo) extends ScalateWebProject(info) {
  }

  class ScalateBookstore(info: ProjectInfo) extends ScalateWebProject(info) {
  }

}
