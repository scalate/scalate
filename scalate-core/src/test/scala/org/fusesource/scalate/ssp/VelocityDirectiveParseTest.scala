package org.fusesource.scalate.ssp

/**
 * @version $Revision : 1.1 $
 */
class VelocityDirectiveParseTest extends ParserTestSupport {
  test("parse if") {
    val lines = assertValid("""<%@ val name: String %>
#if( name == "James")
 Hey James
#end
""")
    assertType(lines(1), classOf[IfFragment])
  }

  test("parse just if expression") {
    val lines = assertValid("""#if( name == "James")
 Hey James
#end
""")
    assertType(lines(0), classOf[IfFragment])
  }

  test("parse just if expression with whitespace before (") {
    val lines = assertValid("""#if (name == "James")
 Hey James
#end
""")
    assertType(lines(0), classOf[IfFragment])
  }

  test("if with nested parens") {

    val lines = assertValid("""some text
#if (foo.bar(123) == "James")
 Hey James
#end
""")
    expect(IfFragment("foo.bar(123) == \"James\"")) { lines(1) }
  }

  test("if with parens in string or char expression") {
    //logging = true

    val lines = assertValid("""some text
#if (foo.bar(")") == ')')
 Hey James
#end
""")
    expect(IfFragment("foo.bar(\")\") == ')'")) { lines(1) }
  }
}