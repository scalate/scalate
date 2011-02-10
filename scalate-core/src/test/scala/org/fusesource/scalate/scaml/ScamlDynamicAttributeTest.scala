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

/**
 * Some tests for Scaml bugs reported
 */
class ScamlDynamicAttributeTest extends ScamlTestSupport {

  val supportDoubleQuotedExpressionsContainingStringLiterals = false

  if (supportDoubleQuotedExpressionsContainingStringLiterals) {
    testRender("#186: dynamic expressions with strings cause issues",
"""%html
  %body
    %a(href="foo.html" title="#{sayHello("Ben")}")
      Hey

""","""<html>
  <body>
    <a href="foo.html" title="Hello Ben">
      Hey
    </a>
  </body>
</html>
""")
  }

  testRender("dynamic attribute expressions with string argument and single quote",
"""%html
  %body
    %a(href="foo.html" title='#{sayHello("James")}')
      Hey

""","""<html>
  <body>
    <a href="foo.html" title="Hello James">
      Hey
    </a>
  </body>
</html>
""")

  testRender("dynamic attribute expressions with string argument and no quotes",
"""%html
  %body
    %a(href="foo.html" title={sayHello("Hiram")})
      Hey

""","""<html>
  <body>
    <a href="foo.html" title="Hello Hiram">
      Hey
    </a>
  </body>
</html>
""")

  override protected def configureTemplateEngine() = {
    engine.importStatements ::= "import org.fusesource.scalate.scaml.SampleSnippets._"
  }
}