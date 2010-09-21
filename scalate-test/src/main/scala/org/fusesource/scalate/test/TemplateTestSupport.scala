package org.fusesource.scalate
package test

import java.io.File
import util.IOUtil

/**
 * A useful base class for testing templates
 */
class TemplateTestSupport extends FunSuiteSupport {
  var engine: TemplateEngine = _
  var showOutput = false

  override protected def beforeAll(configMap: Map[String, Any]) = {
    super.beforeAll(configMap)

    engine = createTemplateEngine
    val workingDir = new File(baseDir, "target/test-data/" + getClass.getSimpleName)
    if (workingDir.exists) {
      // lets delete it before we run the tests
      IOUtil.recursiveDelete(workingDir)
    }
    engine.sourceDirectories = List(new File(baseDir, "src/test/resources"))
    engine.workingDirectory = workingDir
  }

  protected def createTemplateEngine = new TemplateEngine

  def assertUriOutput(expectedOutput: String, uri: String, attributes: Map[String, Any] = Map(), trim: Boolean = false): String =
    assertOutput(expectedOutput, fromUri(uri), attributes, trim)

  def assertOutput(expectedOutput: String, template: TemplateSource, attributes: Map[String, Any] = Map(), trim: Boolean = false): String = {
    var output = engine.layout(template, attributes)
    debug("output: '" + output + "'")

    if (trim) {
      output = output.trim
    }
    expect(expectedOutput) {output}
    output
  }

  def assertOutputContains(source: TemplateSource, expected: String*): String =
    assertOutputContains(source, Map[String, Any](), expected: _*)

  def assertOutputContains(source: TemplateSource, attributes: Map[String, Any], expected: String*): String = {
    var output = engine.layout(source, attributes)
    if (showOutput) {
      println("output: '" + output + "'")
    } else {
      debug("output: '" + output + "'")
    }

    assertTextContains(output, "template " + source, expected: _*)
    output
  }

  def assertUriOutputContains(uri: String, expected: String*): String =
    assertUriOutputContains(uri, Map[String, Any](), expected: _*)

  def assertUriOutputContains(uri: String, attributes: Map[String, Any], expected: String*): String =
    assertOutputContains(fromUri(uri), attributes, expected: _*)

  protected def fromUri(uri: String) = TemplateSource.fromUri(uri, engine.resourceLoader)

  def assertTextContains(source: String, description: => String, textLines: String*): Unit = {
    assume(source != null, "text was null for " + description)
    var index = 0
    for (text <- textLines if index >= 0) {
      index = source.indexOf(text, index)
      if (index >= 0) {
        index += text.length
      }
      else {
        assume(false, "Text does not contain '" + text + "' for " + description + " when text was:\n" + source)
      }
    }
  }
}