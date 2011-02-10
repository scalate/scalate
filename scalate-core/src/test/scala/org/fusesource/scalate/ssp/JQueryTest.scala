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
package org.fusesource.scalate
package ssp


class JQueryTest extends TemplateTestSupport {

  showOutput = true

  test("jQuery generated code") {
    assertOutputContains(TemplateSource.fromText("foo.ssp", """
<p>start of html</p>

<script>
$(document).ready(function(){
  foo.bar();
});

$("a").click(function() {
  alert("Hello world!");
});
</script>

<p>end of html</p>
"""),
      "start of html",
      "$(document).ready",
      """$("a").click""",
      "end of html")
  }

}