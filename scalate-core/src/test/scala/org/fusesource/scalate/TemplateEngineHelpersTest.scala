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

import collection.immutable.Map
import java.io.File
import org.scalatest.ConfigMap

class TemplateEngineHelpersTest extends TemplateTestSupport {

  test("generate URI link for existing file") {
    expect(Some("/moustache.js/array_of_strings.js")) { context.uri(new File(baseDir, "src/test/resources/moustache.js/array_of_strings.js")) }
  }

  test("no link for file outside of source dir") {
    expect(None) { context.uri(new File("/does/not/exist/12345.xml")) }
  }

  def context = new DefaultRenderContext("/foo", engine)

  override protected def beforeAll(configMap: ConfigMap) = {
    super.beforeAll(configMap)

    engine.sourceDirectories = List(new File(baseDir, "src/test/resources"))
  }
}