package org.fusesource.scalate.util

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ClassLoadersTest extends FunSuite {
  import ClassLoaders.AntLikeClassLoader

  test("AntLikeClassLoader extractor matches classes with a 'def getClasspath: String' method") {
    val hasMethod = new ClassWithGetClasspathMethod
    
    assert(AntLikeClassLoader.unapply(hasMethod) === Some(hasMethod))
    
    assert(AntLikeClassLoader.unapply("I don't have a getClasspath method") === None)
  }
  
  class ClassWithGetClasspathMethod {
    def getClasspath: String = ""
  }
}
