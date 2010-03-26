package org.fusesource.scalate.util

import _root_.org.fusesource.scalate.FunSuiteSupport
import java.io.File
import java.net.{URL, URLClassLoader}

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
  
  test("Add enty from a URLClassLoader") {
    val loader = new URLClassLoader(Array(new URL("file:///path/to/file.jar")))
    val builder = new ClassPathBuilder
    builder.addPathFrom(loader)
    assertFiles(builder.classPath, "/path/to/file.jar")
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

  def assertFiles(actualPath: String, expectedPath: String) = {
    val actualFile = new File(actualPath)
    val expectedFile = new File(expectedPath)
    assert(actualFile.getCanonicalPath === expectedFile.getCanonicalPath)
  }
}

/*
 * Test data
 */
object ClassPathBuilderTest {
  
  def testLibDir = new java.io.File(getClass.getClassLoader.getResource("test-lib").toURI).getParent
  
  object ValidAntLikeClassLoader extends ClassLoader(null) {
    def getClasspath: String = "/path/to/file.jar"
  }
  
  object InvalidAntLikeClassLoader extends ClassLoader(null) {
    def getClasspath: Int = 42
  }
}
