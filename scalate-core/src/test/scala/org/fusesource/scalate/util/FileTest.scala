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

package org.fusesource.scalate.util

import org.fusesource.scalate.FunSuiteSupport
import java.io.File
import IOUtil._

class FileTest extends FunSuiteSupport {

  test("using rich file API to navigate") {
    val f: File = baseDir

    val classes = f / "target" / "classes"

    expect(true) {classes.exists}

    val f2: File = classes
    expect(true) {f2.exists}

    info("created file: " + classes.file)
  }

  test("getting text of a file") {
    val file: File = baseDir / "src/test/resources/dummy.txt"

    val t = file.text.trim
    expect("hello world!"){t}

    info("Loaded file: " + file + " as text: " + t)
  }

  test("working with names") {
    val file = baseDir / "foo.txt"

    expect("txt", "extension") {file.extension}
    expect("foo", "nameDropExtension") {file.nameDropExtension}

    println("name: " + file.name + " extension: " + file.extension)
  }

  test("Finding files") {
    expect(None) {
      baseDir.find(_.name == "doesNotExist.xml")
    }

    expect(Some(new File(baseDir, "pom.xml"))) {
      baseDir.find(_.name == "pom.xml")
    }

    expect(Some(new File(baseDir, "src/test/scala/org/fusesource/scalate/util/FileTest.scala"))) {
      baseDir.find(_.name == "FileTest.scala")
    }

  }
}