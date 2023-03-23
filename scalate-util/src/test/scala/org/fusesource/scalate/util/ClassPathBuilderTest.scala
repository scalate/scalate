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

import java.io.File
import java.net.{ URL, URLClassLoader }

class ClassPathBuilderTest extends FunSuiteSupport {
  import ClassPathBuilderTest._

  test("Construct an empty class path") {
    val builder = new ClassPathBuilder
    assert(builder.classPath === "")
  }

  test("All methods accept null") {
    val builder = new ClassPathBuilder
    builder.addClassesDir(null)
      .addLibDir(null)
      .addJar(null)
      .addPathFrom(null: Class[_])
      .addPathFrom(null: ClassLoader)

    assert(builder.classPath === "")
  }

  test("Add an entry for a jar file") {
    val builder = new ClassPathBuilder
    builder.addJar("/path/to/file.jar")

    assertFiles(builder.classPath, "/path/to/file.jar")
  }

  test("Add an entry for a classes directory") {
    val builder = new ClassPathBuilder
    builder.addClassesDir("/WEB-INF/classes")
    assert(builder.classPath === "/WEB-INF/classes")
  }

  test("Entries are added sequentially") {
    val builder = new ClassPathBuilder
    builder.addClassesDir("/WEB-INF/classes")
    builder.addJar("/WEB-INF/lib/scalate.jar")
    val classPath = builder.classPath
    assert(classPath.indexOf("classes") < classPath.indexOf("scalate"))
  }

  test("Duplicates are removed from the class path") {
    val builder = new ClassPathBuilder
    builder.addClassesDir("/WEB-INF/classes")
    builder.addClassesDir("/WEB-INF/classes")
    assert(builder.classPath === "/WEB-INF/classes")
  }

  //  Fails from Maven :-( commented out as unstable
  //  test("Add the entries form java.class.path system property") {
  //    val builder = new ClassPathBuilder
  //    builder.addJavaPath
  //    assert(builder.classPath.contains("scala"))
  //    // We assume that the Scala jar is in the class path
  //  }

  test("Add entry from a URLClassLoader") {
    val loader = new URLClassLoader(Array(new URL("file:///path/to/file.jar")))
    val parentClassPathBuilder = new ClassPathBuilder
    parentClassPathBuilder.addPathFrom(getClass.getClassLoader)

    val builder = new ClassPathBuilder
    builder.addPathFrom(loader)

    assertFiles(
      builder.classPath.split(File.pathSeparator).toList,
      parentClassPathBuilder.classPath.split(File.pathSeparator).filter(_.nonEmpty).+:("/path/to/file.jar").toList)
  }

  test("Add path from AntLikeClassLoader") {
    val builder = new ClassPathBuilder

    builder.addPathFrom(InvalidAntLikeClassLoader)
    assert(builder.classPath === "")

    builder.addPathFrom(ValidAntLikeClassLoader)
    assertFiles(builder.classPath, "/path/to/file.jar")
  }

  test("Add path from context class loader") {

    val contextClassLoader = Thread.currentThread.getContextClassLoader
    val builder = new ClassPathBuilder

    Thread.currentThread.setContextClassLoader(null)
    builder.addPathFromContextClassLoader()
    assert(builder.classPath === "")

    Thread.currentThread.setContextClassLoader(ValidAntLikeClassLoader)
    builder.addPathFromContextClassLoader()
    assertFiles(builder.classPath, "/path/to/file.jar")

    Thread.currentThread.setContextClassLoader(contextClassLoader)
  }

  test("Add jars for a lib directory") {
    val builder = new ClassPathBuilder
    builder.addLibDir(testLibDir)
    assert(builder.classPath.contains("fake-jar"))
  }

  test("Contains the classpaths of all class loaders including parents") {
    assume(!System.getProperty("os.name").toLowerCase.contains("windows"))

    val builder = new ClassPathBuilder

    Thread.currentThread.setContextClassLoader(ValidChildClassLoader)

    builder.addPathFromContextClassLoader()

    val expectFiles = Seq(
      ValidChildClassLoader.getClasspath,
      ValidAntLikeClassLoader.getClasspath)
    builder.classPath.split(File.pathSeparator).foreach { path =>
      assert(expectFiles.contains(new File(path).getCanonicalPath))
    }
  }

  def assertFiles(actualPath: String, expectedPath: String) = {
    val actualFile = new File(actualPath)
    val expectedFile = new File(expectedPath)
    assert(actualFile.getCanonicalPath === expectedFile.getCanonicalPath)
  }

  def assertFiles(actualPaths: List[String], expectedPaths: List[String]) = {
    val actualFile = actualPaths.map(new File(_).getCanonicalPath)
    val expectedFile = expectedPaths.map(new File(_).getCanonicalPath)
    assert(actualFile === expectedFile)
  }
}

/*
 * Test data
 */
object ClassPathBuilderTest {

  def testLibDir = new java.io.File(getClass.getClassLoader.getResource("test-lib").toURI).getParent

  object ValidChildClassLoader extends ClassLoader(ValidAntLikeClassLoader) {
    def getClasspath: String = "/path/to/child.jar"
  }

  object ValidAntLikeClassLoader extends ClassLoader(null) {
    def getClasspath: String = "/path/to/file.jar"
  }

  object InvalidAntLikeClassLoader extends ClassLoader(null) {
    def getClasspath: Int = 42
  }
}
