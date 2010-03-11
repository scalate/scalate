package org.fusesource.scalate.ssp

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import collection.mutable.HashMap
import org.scalatest.FunSuite

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ParserTest extends FunSuite {
  val logging = false

  test("parse attribute declaration") {
    val lines = assertValid("""<%@ val name: String %>
<html>
  <%-- comment --%>
  <body>
    <h1>Hello ${user > 5}</h1>
    <p>Hey <%= blah.somthing("$blah") % 5 %></p>
  </body>
</html>
""")

    assertAttribute(lines, AttributeFragment("val", "name", "String", None, false))
  }

// Failing test!
// In the current SspParser implementation we cannot have whitespace in the default value
//  test("parse attribute declaration with the default value being a constructor invocation") {
//    val lines = assertValid("""<%@ val name: String = new String("spiros")%>
//<html>
//  <%-- comment --%>
//  <body>
//    <h1>Hello ${name}</h1>
//  </body>
//</html>
//""")
//    assertAttribute(lines, AttributeFragment("val", "name", "String", Some("new String(\"spiros\")"), false))
//  }
  

  test("parse valid SSP file with attribute with default value") {
    val lines = assertValid("""<%@ val name : String = "Hello"%>
<html>
  <body>
    <h1>Hello ${user}</h1>
    <p>Hey</p>
  </body>
</html>
""")

    assertAttribute(lines, AttributeFragment("val", "name", "String", Some("\"Hello\""), false))
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

    val lines = (new SspParser).getPageFragments(text)
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