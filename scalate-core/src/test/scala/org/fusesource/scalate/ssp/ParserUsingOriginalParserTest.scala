package org.fusesource.scalate.ssp

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import collection.mutable.HashMap
import org.scalatest.FunSuite

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ParserUsingOriginalParserTest extends FunSuite {
  val logging = false

  test("parse attribute declaration") {
    val lines = assertValid("""<%@ attribute name: String %>
<html>
  <%-- comment --%>
  <body>
    <h1>Hello ${user > 5}</h1>
    <p>Hey <%= blah.somthing("$blah") % 5 %></p>
  </body>
</html>
""")

    assertAttribute(lines, AttributeFragment("name", "String", None))
  }

  test("parse valid SSP file with attribute with default value") {
    val lines = assertValid("""<%@ attribute name : String = "Hello"%>
<html>
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")

    assertAttribute(lines, AttributeFragment("name", "String", Some("\"Hello\"")))
  }


  test("parse valid SSP file with special symbols in code") {
    val lines = assertValid("""<%
    import scala.util.xml._ %>
<html>
  <%-- comment --%>
  <body>
    <h1>Hello ${user > 5}</h1>
    <p>Hey <%= blah.somthing("$blah") % 5 %></p>
  </body>
</html>
""")

    val count = countTypes(lines)
    expect(1) {count(classOf[CommentFragment])}
    expect(1) {count(classOf[ScriptletFragment])}
    expect(1) {count(classOf[DollarExpressionFragment])}
    expect(1) {count(classOf[ExpressionFragment])}
    lines
  }

  test("parse valid SSP files without newlines") {
    assertValid("""<% import scala.util.xml._ %>
<html>
  <%-- comment --%>
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")
  }



  test("parse simple SSP without tags") {
    assertValid("""hey <% import scala.util.xml._ %> ${foo} <%-- comment --%> yo""")
  }

  test("parse valid SSP files") {
    val lines = assertValid("""<%
    import scala.util.xml._
%>
<html>
  <%-- comment --%>
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")
    lines
  }

  test("parse simple valid SSP files") {
    assertValid("""<html>
  <%-- comment --%>
  <body>
    <h1>Hello <%= user %> how are you ${when}</h1>
    <p>Hey</p>
  </body>
</html>
""")
  }

  def countTypes(lines: List[PageFragment]): HashMap[Class[_], Int] = {
    val map = new HashMap[Class[_], Int]
    for (line <- lines) {
      val key = line.getClass
      map(key) = map.getOrElse(key, 0) + 1
    }
    map
  }


  def assertAttribute(lines: List[PageFragment], expectedParam: AttributeFragment) = {
    val attribute = lines.find {
      case d: AttributeFragment => true
      case _ => false
    }
    expect(Some(expectedParam)) {attribute}

    lines
  }

  def assertValid(text: String): List[PageFragment] = {
    log("Parsing...")
    log(text)
    log("")

    val lines = SspOriginalParser.parseFragments(text)
    for (line <- lines) {
      log(line)
    }
    log("")
    lines
  }

  def log(value: AnyRef) {
    if (logging) {
      println(value)
    }
  }
}