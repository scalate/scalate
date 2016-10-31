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
package org.fusesource.scalate.util

import _root_.org.fusesource.scalate.FunSuiteSupport
import org.fusesource.scalate.util.URIs._

/**
 * @version $Revision: 1.1 $
 */
class URIsTest extends FunSuiteSupport {

  test("adding query argument to no query string") {
    expect("/foo?x=1") { uri("/foo", "x=1") }
  }

  test("adding query argument to query string") {
    expect("/foo?x=1&y=2") { uri("/foo?x=1", "y=2") }
  }

  test("adding query argument to existing query") {
    expect("/foo?x=1&y=2") { uriPlus("/foo", null, "x=1&y=2") }
    expect("/foo?x=1&y=2") { uriPlus("/foo", "", "x=1&y=2") }
    expect("/foo?x=1&y=2") { uriPlus("/foo", "x=1", "y=2") }
    expect("/foo?x=1&y=2") { uriPlus("/foo", "x=1", "x=1&y=2") }
    expect("/foo?x=1&y=2") { uriPlus("/foo", "x=1&y=2", "x=1&y=2") }
  }

  test("removing query argument to existing query") {
    expect("/foo?x=1&y=2") { uriMinus("/foo", "x=1&y=2", "foo=bar") }
    expect("/foo?x=1&y=2") { uriMinus("/foo", "x=1&y=2&z=3", "z=3") }
  }
}