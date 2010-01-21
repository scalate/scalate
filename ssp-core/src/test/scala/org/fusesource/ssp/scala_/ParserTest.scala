package org.fusesource.ssp.scala_

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ParserTest extends FunSuite {

  val parser = new SspParser()
  
  test("parse simple SSP without tagsf") {
    assertValid("""hey <% import scala.util.xml._ %> ${foo} <%-- comment --%> yo""")
  }

  test("parse valid SSP files") {
    assertValid("""<%
    import scala.util.xml._
%>
<html>
  <%-- comment -->
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")
  }

  test("parse valid SSP files without newlines") {
    assertValid("""<% import scala.util.xml._ %>
<html>
  <%-- comment -->
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")
  }

  test("parse simple valid SSP files") {
    assertValid("""<html>
  <%-- comment -->
  <body>
    <h1>Hello <%= user %> how are you ${when}</h1>
    <p>Hey</p>
  </body>
</html>
""")
  }

  test("parse valid SSP file with param") {
    assertValid("""<%@ param name : String = "Hello"%>
<html>
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")
  }


  def assertValid(text: String): Unit = {
    println("Parsing...")
    println(text)
    println
    
    parser.parse(text) match {
      case parser.Success(lines : List[PageFragment], _) =>
        for (line <- lines) {
          println(line)
        }
      case e =>
        fail("Failed to parse! " + e)
    }
    println
  }
}