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
package org.fusesource.scalate.support

import org.fusesource.scalate.FunSuiteSupport

class ConvertAbsoluteLinkTest extends FunSuiteSupport {
  List(
    "http://fusesource.com",
    "foo.html",
    "bar/whatnot/foo.html") foreach assertUnchanged

  assertChanged("/foo.html", "/bar.html", "foo.html")
  assertChanged("/foo.html", "/a/bar.html", "../foo.html")
  assertChanged("/foo.html", "/a/b/bar.html", "../../foo.html")
  assertChanged("/a/foo.html", "/a/bar.html", "foo.html")
  assertChanged("/a/foo.html", "/b/bar.html", "../a/foo.html")
  assertChanged("/a/foo.html", "/b/c/d/bar.html", "../../../a/foo.html")
  assertChanged("/a/b/foo.html", "/a/c/d/bar.html", "../../b/foo.html")

  protected def assertChanged(link: String, requestUri: String, expected: String): Unit = {
    test(link + " from " + requestUri) {
      val answer = Links.convertAbsoluteLinks(link, requestUri)

      logger.info("should convert " + link + " at " + requestUri + " -> " + answer)
      assertResult(expected) { answer }
    }
  }

  protected def assertUnchanged(link: String): Unit = {
    test(link) {
      val answer = Links.convertAbsoluteLinks(link, "/foo/bar.html")

      logger.info("should be unchanged " + link + " -> " + answer)
      assertResult(link, "Should not be changed") { answer }
    }
  }
}
