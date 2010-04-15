package org.fusesource.scalate.ssp

import org.fusesource.scalate.{CompilerException, TemplateTestSupport}

/**
 * @version $Revision : 1.1 $
 */
class VelocityDirectiveTest extends TemplateTestSupport {
  test("for loop") {
    assertSspOutput("xxx", "#for(i <- 1 to 3)x#end")
  }

  test("if elseif else") {
    val template = compileSsp("if elseif else", """<%@ val n: String %>
#if (n == "James")
Hey James
#elseif (n == "Hiram")
Yo Hiram
#else
Dunno
#end
""")

    assertTrimOutput("Hey James", template, Map("n" -> "James"))
    assertTrimOutput("Yo Hiram", template, Map("n" -> "Hiram"))
    assertTrimOutput("Dunno", template, Map("n" -> "Foo"))
  }

  // TODO test match / case / otherwise
  ignore("match case otherwise") {
    val template = compileSsp("match case otherwise", """<%@ val n: String %>
#match(n)
#case("James")
Hey James
#case("Hiram")
Yo Hiram
#otherwise
Dunno
#end
""")

    assertTrimOutput("Hey James", template, Map("n" -> "James"))
    assertTrimOutput("Yo Hiram", template, Map("n" -> "Hiram"))
    assertTrimOutput("Dunno", template, Map("n" -> "Foo"))
  }

  test("import test") {
    val template = compileSsp("import test", """
#import(java.util.Date)
time is: ${new Date()}
""")
    val output = engine.layout(template).trim
    assert(output.startsWith("time is:"))
  }


  testSspSyntaxEception("missing #end", "#for(i <- 1 to 3) blah")
  testSspSyntaxEception("too many #end", "#for(i <- 1 to 3) blah #end #end")

  // check incorrect nesting...
  testSspSyntaxEception("#elseif without #if", "#for(i <- 1 to 3) #elseif(x > 5) #end")
  testSspSyntaxEception("#else without #if", "#for(i <- 1 to 3) #else #end")

  testSspSyntaxEception("#case without #match", "#for(i <- 1 to 3) #case(x) #end")
  testSspSyntaxEception("#otherwie without #match", "#for(i <- 1 to 3) #otherwise #end")

  // check that we don't use #elseif or #case after #otherwise or #else
  testSspSyntaxEception("#else after #elseif", "#if(x > 5) a #else b #elseif(x < 1) c #end")
  testSspSyntaxEception("#otherwise after #case", "#match(x) #otherwise b #case(5) c #end")


  // check that we don't use multiple #else or #otherwise
  testSspSyntaxEception("too many #else", "#if(x > 5) a #elseif(x < 1) z #else b #else c #end")
  testSspSyntaxEception("too many #else without #eliseif", "#if(x > 5) a #else b #else c #end")
  testSspSyntaxEception("too many #otherwise", "#match(x) #case(5) a #otherwise b #otherwise c #end")
  testSspSyntaxEception("too many #otherwise without #case", "#match(x) #otherwise b #otherwise c #end")

  // check that we can open/close clauses within a parent close
  test("nested if statements") {
    assertTrimSspOutput("worked", """<%@ val x: Int %>
#if (x < 1)
foo
#else
  #if (x < 1)
  foo2
  #elseif (x > 4)
  worked
  #else
  foo3
  #end
#end
""", Map("x" -> 5))
  }

}