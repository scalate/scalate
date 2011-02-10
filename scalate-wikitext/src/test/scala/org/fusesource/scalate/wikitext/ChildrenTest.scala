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
package org.fusesource.scalate.wikitext

import org.fusesource.scalate.test.TemplateTestSupport
import java.io.File

class ChildrenTest extends TemplateTestSupport {

  test("children depth 1") {
    assertUriOutputContains("foo.conf", """<a href="foo/a.conf">""",
      """<a href="foo/b.conf">""",
      """<a href="foo/user-guide.conf">""", "User Guide")
  }


  test("children all") {
    assertUriOutputContains("all.conf", """<a href="foo/a.conf">""", """<a href="foo/b.conf">""",
      """<a href="foo/b/b1.conf">""", """<a href="foo/b/b2.conf">""")
  }


  test("children of b") {
    assertUriOutputContains("foo/b.conf", """<a href="b/b1.conf">""", """<a href="b/b2.conf">""")
  }


  override protected def beforeAll(map: Map[String, Any]) = {
    super.beforeAll(map)

    engine.sourceDirectories = List(new File(baseDir, "src/test/resources"))
  }
}