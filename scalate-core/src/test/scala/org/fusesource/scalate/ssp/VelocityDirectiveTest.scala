/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.scalate.ssp

import org.fusesource.scalate.{CompilerException, TemplateTestSupport}

/**
 * @version $Revision : 1.1 $
 */
class VelocityDirectiveTest extends TemplateTestSupport {
  test("escape of #") {
    assertSspOutput("""hey # and \# foo""", """hey \# and \\# foo""")
    assertSspOutput("""#if (foo) blah #end""", """\#if (foo) blah \#end""")
  }

  test("for loop") {
    assertSspOutput("1 2 3 ", "#for(i <- 1 to 3)${i} #end")
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

  
  test("match case otherwise") {
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
    val output = engine.layout("foo1.ssp", template).trim
    assert(output.startsWith("time is:"))
  }

  test("import using block") {
    val template = compileSsp("import using block", """
#{
  import java.util.Date
}#
time is: ${new Date()}
""")
    val output = engine.layout("foo2.ssp", template).trim
    assert(output.startsWith("time is:"))
  }

  test("do with no expression works ok") {
    val template = compileSsp("do with no expression", """
start
#do()
foo
#end
end
""")
    val output = engine.layout("foo3.ssp", template).trim
    expect(List("start", "foo", "end")) { output.split("\\s+").toList }
  }


  // #match and #case issues
  testSspSyntaxEception("non whitespace between #match #case", "#match(n) bad #case(5) a #otherwise b #end")
  testSspSyntaxEception("cannot have other directive between #match #case", "#match(n) #if(5) #case(5) a #otherwise b #end")

  // correct use of #end
  testSspSyntaxEception("missing #end", "#for(i <- 1 to 3) blah")
  testSspSyntaxEception("too many #end", "#for(i <- 1 to 3) blah #end #end")

  // check incorrect nesting...
  testSspSyntaxEception("#elseif without #if", "#for(i <- 1 to 3) #elseif(x > 5) #end")
  testSspSyntaxEception("#else without #if", "#for(i <- 1 to 3) #else #end")

  testSspSyntaxEception("#case without #match", "#for(i <- 1 to 3) #case(x) #end")
  testSspSyntaxEception("#otherwie without #match", "#for(i <- 1 to 3) #otherwise #end")

  // check that we don't use #elseif or #case after #otherwise or #else
  testSspSyntaxEception("#else after #elseif", "#if(x > 5) a #else b #elseif(x < 1) c #end")
  testSspSyntaxEception("#else after #elseelseif", "#if(x > 5) a #else b #else c #end")
  testSspSyntaxEception("#otherwise after #case", "#match(x) #otherwise b #case(5) c #end")
  testSspSyntaxEception("#otherwise after #otherwise", "#match(x) #otherwise b #otherwise c #end")

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