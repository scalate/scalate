package org.fusesource.scalate
package wikitext

import org.fusesource.scalate.test.FunSuiteSupport

/**
 * Abstract base class to build Confluence wiki markup test classes 
 */
abstract class AbstractConfluenceTest extends FunSuiteSupport {

  val filter = ConfluenceFilter

  protected def renderConfluence(source: String): String = {
    debug("Converting: " + source)
    val context = new DefaultRenderContext("foo.conf", new TemplateEngine())
    val actual = filter.filter(context, source)
    info("Generated: " + actual)
    println
    println(actual)
    actual
  }

  protected def assertFilter(source: String, expected: String): Unit = {
    val actual: String = renderConfluence(source)
    expect(expected) {actual}
  }    
}