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

package org.fusesource.scalate
package rest

import collection.immutable.Map
import java.io.File
import support.DefaultTemplatePackage

class DefaultTemplatePackageTest extends TemplateTestSupport {

  test("template using 'it' attribute auto defined by ScalatePackage") {
    val attributes = Map("it" -> SomeObject("James", "Mells"))
    assertUriOutputContains("/org/fusesource/scalate/rest/SomeObject.index.ssp", attributes,
      "name: James town: Mells")
  }

  override protected def beforeAll(configMap: Map[String, Any]) = {
    super.beforeAll(configMap)
    engine.sourceDirectories = List(new File(baseDir, "src/test/resources"))
  }
}

case class SomeObject(name: String, town: String) {
}

class SamplePackage extends DefaultTemplatePackage {
}