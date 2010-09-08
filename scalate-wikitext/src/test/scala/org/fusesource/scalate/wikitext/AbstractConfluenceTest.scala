package org.fusesource.scalate.wikitext

import org.fusesource.scalate.test.FunSuiteSupport

/**
 * Abstract base class to build Confluence wiki markup test classes 
 */
abstract class AbstractConfluenceTest extends FunSuiteSupport {

  val filter = ConfluenceFilter

  protected def assertFilter(source: String, expected: String): Unit = {
    println("Converting: " + source)
    val actual = filter.filter(source)
    println("Created: " + actual)
    expect(expected) {actual}
  }    
}