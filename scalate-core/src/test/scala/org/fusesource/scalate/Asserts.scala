package org.fusesource.scalate

/**
 * @version $Revision : 1.1 $
 */

object Asserts {
  def assertContains(actual: String, expected: String): Unit = {
    assert(actual.contains(expected), "Should contain \"" + expected + "\" but was: " + actual)
  }

  def benchmark(name: String)(block: => Unit) = {
    val start = System.currentTimeMillis
    block
    val end = System.currentTimeMillis - start
    println(name + " " + end  + " milli(s)")
  }
}