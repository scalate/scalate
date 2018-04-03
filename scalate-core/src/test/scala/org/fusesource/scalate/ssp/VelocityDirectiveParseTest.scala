/**
 * Copyright (C) 2009-2011 the original author or authors.
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
    assertResult(IfFragment("foo.bar(123) == \"James\"")) { lines(1) }
  }

  test("if with parens in string or char expression") {
    //logging = true

    val lines = assertValid("""some text
#if (foo.bar(")") == ')')
 Hey James
#end
""")
    assertResult(IfFragment("foo.bar(\")\") == ')'")) { lines(1) }
  }
}
