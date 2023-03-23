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
package org.fusesource.scalate.sample

import _root_.java.io.File

import _root_.org.fusesource.scalate._
import _root_.org.fusesource.scalate.test.FunSuiteSupport
import _root_.org.junit.runner.RunWith
import _root_.org.scalatestplus.junit.JUnitRunner
import com.google.common.io.Files

case class Person(first: String, last: String)

@RunWith(classOf[JUnitRunner])
class ArchetypeTest extends FunSuiteSupport {
  val engine = new TemplateEngine

  // If the version number is not added, class files of different major versions will be reused,
  // so the compile-time version number is added to the directory name to distinguish them.
  val ver = buildinfo.BuildInfo.scalaVersion
  engine.workingDirectory = new File(baseDir, "target/test-data/ArchetypeTest" + ver)

  test("use tableView archetype") {
    val output = engine.layout("/WEB-INF/scalate/archetypes/views/index/tableView.ssp", Map("resourceType" -> classOf[Person])).trim

    log.info("Generated SSP:")
    log.info(output)
  }
}
