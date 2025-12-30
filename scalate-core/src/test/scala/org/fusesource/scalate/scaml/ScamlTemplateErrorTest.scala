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
package org.fusesource.scalate.scaml

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ScamlTemplateErrorTest extends ScamlTestSupport {
  val scalaV = org.fusesource.scalate.buildinfo.BuildInfo.scalaVersion

  if (scalaV.startsWith("3")) {
    testCompilerException(
      "Compile Error",
      """
%html
%body
%ul
  - for (i <- unknown)
    %li= i
""",
      "Not found: unknown")
  } else {
    testCompilerException(
      "Compile Error",
      """
%html
  %body
    %ul
      - for (i <- unknown)
        %li= i
""",
      "not found: value unknown")
  }

  /////////////////////////////////////////////////////////////////////
  //
  // Tests for indentation inconsistencies
  //
  /////////////////////////////////////////////////////////////////////

  testRender(
    "valid indenting",
    """
%html
  %two
    %three
  %two
""", """
<html>
  <two>
    <three></three>
  </two>
  <two></two>
</html>
""")

  testInvalidSyntaxException(
    "Inconsistent indent level detected: indented too shallow",
    """
%html
  %two
   %tooshallow
  %two
""",
    "Inconsistent indent level detected: indented too shallow at 3.4")

  testInvalidSyntaxException(
    "Inconsistent indent level detected: indented too shallow at root",
    """
%html
  %two
 %toodeep
  %two
""",
    "Inconsistent indent level detected: indented too shallow at 3.2")

  testInvalidSyntaxException(
    "Inconsistent indent level detected: indented too deep",
    """
%html
  %two
     %toodeep
  %two
""",
    "Inconsistent indent level detected: indented too deep at 3.6")

  testInvalidSyntaxException(
    "Inconsistent indent detected: indented with spaces but previous lines were indented with tabs",
    """
%html
	%tab
  %spaces
	%tab
""",
    "Inconsistent indent detected: indented with spaces but previous lines were indented with tabs at 3.3")

  testInvalidSyntaxException(
    "Unexpected comma in html attribute list",
    """
%html
%tab(comma="common", error="true")
%p commas in attribute lists is a common errro
""",
    "')' expected but ',' found at 2.20")

}
