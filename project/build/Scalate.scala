import sbt._

// TODO disable WebbyTest until its in a maven repo
//import webbytest.HtmlTestsProject

/**
 * @version $Revision : 1.1 $
 */
class ScalateProject(info: ProjectInfo) extends ParentProject(info) {

  // use local maven repo
  val mavenLocal = "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"

  // Projects
  lazy val core = project("scalate-core", "Scalate Core", new Core(_))
  lazy val test = project("scalate-test", "Scalate Test", new Test(_))
  lazy val camel = project("scalate-camel", "Scalate Camel", new Camel(_), core, test)
  lazy val war = project("scalate-war", "Scalate WAR Overlay", new War(_), core, test)
  lazy val sample = project("scalate-sample", "Scalate Sample Web App", new Sample(_), core, test, war)
  lazy val bookstore = project("scalate-bookstore", "Scalate Bookstore Sample Web App", new Bookstore(_), core, test, war)


  // TODO disable WebbyTest until its in a maven repo
  class Core(info: ProjectInfo) extends DefaultProject(info) { // TODO with HtmlTestsProject {
    println("core project uses " + testScalaSourcePath)
  }

  class Test(info: ProjectInfo) extends DefaultProject(info) {
  }

  class Camel(info: ProjectInfo) extends DefaultProject(info) {
  }

  class War(info: ProjectInfo) extends DefaultWebProject(info) {
  }

  class Sample(info: ProjectInfo) extends DefaultWebProject(info) {
  }

  class Bookstore(info: ProjectInfo) extends DefaultWebProject(info) {
  }

}
