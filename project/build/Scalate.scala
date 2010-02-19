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
  lazy val camel = project("scalate-camel", "Scalate Camel", new Camel(_), core)
  lazy val sample = project("scalate-sample", "Scalate Sample Web App", new Sample(_), core)
  lazy val bookstore = project("scalate-bookstore", "Scalate Bookstore Sample Web App", new Bookstore(_), core)


  // TODO disable WebbyTest until its in a maven repo
  class Core(info: ProjectInfo) extends DefaultProject(info) { // TODO with HtmlTestsProject {
  }

  class Camel(info: ProjectInfo) extends DefaultProject(info) {
  }

  class Sample(info: ProjectInfo) extends DefaultWebProject(info) {
  }

  class Bookstore(info: ProjectInfo) extends DefaultWebProject(info) {
  }

  lazy val cleanPlugins = task {
    Console.println(info.pluginsPath)
    val paths = info.pluginsPath / "target" ::
      info.pluginsPath / "src_managed" ::
      info.pluginsPath / "lib_managed" ::
      info.pluginsPath / "project" :: Nil
    FileUtilities.clean(paths, true, log)
    None
  }
}
