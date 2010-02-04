import sbt._

import webbytest.HtmlTestsListener

/**
 * @version $Revision : 1.1 $
 */
class ScalateProject(info: ProjectInfo) extends ParentProject(info) {



  // use local maven repo
  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

  // Projects
  lazy val core = project("scalate-core", "Scalate Core", new Core(_))
  lazy val camel = project("scalate-camel", "Scalate Camel", new Camel(_), core)
  lazy val sample = project("scalate-sample", "Scalate Sample Web App", new Sample(_), core)


  class Core(info: ProjectInfo) extends DefaultProject(info) {
    // use nicer test reporting
    override def testListeners: Seq[TestReportListener] = new HtmlTestsListener("scalate-core/target/tests.html") :: Nil
    //override def testListeners: Seq[TestReportListener] = (new HtmlTestsListener() :: Nil) ++ super.testListeners
  }

  class Camel(info: ProjectInfo) extends DefaultProject(info) {
  }

  class Sample(info: ProjectInfo) extends DefaultWebProject(info) {
  }

}
