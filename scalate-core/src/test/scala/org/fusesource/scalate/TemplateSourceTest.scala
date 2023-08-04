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

class TemplateSourceTest extends FunSuiteSupport {
  test("extract extension from uri") {
    val source = TemplateSource.fromFile("foo.ssp")
    assertResult(Some("ssp")) { source.templateType }
  }

  test("override extension") {
    val source = TemplateSource.fromFile("foo.txt").templateType("mustache")
    assertResult(Some("mustache")) { source.templateType }
  }

  test("empty package name") {
    val engine = new TemplateEngine
    val source = TemplateSource.fromFile("foo.ssp")
    source.engine = engine
    assertResult("") { source.packageName }
  }

  test("illegal package name - first token") {
    val engine = new TemplateEngine
    val source = TemplateSource.fromFile("/var/tmp2/foo.ssp")
    source.engine = engine
    assertResult("tmp2") { source.packageName }
  }

  test("illegal package name - second token") {
    val engine = new TemplateEngine
    val source = TemplateSource.fromFile("/tmp/var/tmp2/foo.ssp")
    source.engine = engine
    assertResult("tmp2") { source.packageName }
  }
}
