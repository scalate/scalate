package org.fusesource.scalate.console

/**
 * @version $Revision: 1.1 $
 */
import _root_.java.io.{OutputStreamWriter, PrintWriter}
import org.fusesource.scalate._
import org.fusesource.scalate.util._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EditLinkTest extends FunSuite with Logging {

  val file = "src/test/scala/org/fusesource/scalate/console/EditLinkTest.scala"

  test("default edit link") {
    editLink("default")
  }

  test("IDE edit link") {
    System.setProperty("scalate.editor", "ide")
    editLink("ide")
  }

  def editLink(name: String) = {

    // lets put a render context in scope
    RenderContext() = new DefaultRenderContext(new TemplateEngine(), new PrintWriter(new OutputStreamWriter(System.out)))
    
    val link = EditLink.editLink(file)("Edit file")

    println(name + " link = " + link)
  }
}