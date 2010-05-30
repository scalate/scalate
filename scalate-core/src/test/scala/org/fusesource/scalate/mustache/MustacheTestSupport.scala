package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.{TemplateEngine, TemplateSource, TemplateTestSupport}
import org.fusesource.scalate.util.IOUtil
import collection.immutable.Map

/**
 * Base class for Mustache test cases based on mustache.js and mustache.ruby test cases
 */
abstract class MustacheTestSupport extends TemplateTestSupport {
  var trimOutputAndTemplate = true

  def mustacheTest(name: String, attributes: Map[String, Any]): Unit = mustacheTest(name, "", attributes)

  def mustacheTest(name: String, description: String, attributes: Map[String, Any]): Unit = {
    test(name + " " + description) {
      assertMustacheTest(name, attributes)
    }
  }

  protected def assertMustacheTest(name: String, attributes: Map[String, Any]): Unit = {
    debug("Using template reasource loader: " + engine.resourceLoader)

    val template = engine.load(engine.source(name + ".html", "mustache"))
    val expectedOutput = IOUtil.loadTextFile(new File(rootDir, name + ".txt"))
    if (trimOutputAndTemplate) {
      assertTrimOutput(expectedOutput.trim, template, attributes)
    }
    else {
      assertOutput(expectedOutput, template, attributes)
    }
  }

  override protected def createTemplateEngine = {
    debug("Using rootDir: " + rootDir)
    new TemplateEngine(Some(rootDir))
  }

  def rootDir = new File(baseDir, "src/test/resources/moustache/js")
}