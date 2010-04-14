package org.fusesource.scalate.ssp

import _root_.org.fusesource.scalate.FunSuiteSupport
import collection.mutable.HashMap

/**
 * @version $Revision : 1.1 $
 */
class ParserTest extends ParserTestSupport {

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

  test("parse attribute declaration with the default value being a constructor invocation") {
    val lines = assertValid("""<%@ val name: String = new String("spiros")%>
<html>
  <%-- comment --%>
  <body>
    <h1>Hello ${name}</h1>
  </body>
</html>
""")
    assertAttribute(lines, AttributeFragment("val", "name", "String", Some("new String(\"spiros\")"), false))
  }

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

}