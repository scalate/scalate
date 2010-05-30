package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.util.IOUtil
import util.parsing.json.JSON

/**
 * Runs the system tests from the mustache.js distro
 */
class MustacheJSONTest extends MustacheTestSupport {

  // Note that these JS files must be valid JSON so using " quoted names

  mustacheJsonTest("array_of_strings")
  mustacheJsonTest("array_of_strings_options")
  mustacheJsonTest("inverted_section")
  mustacheJsonTest("template_partial")


  // Implementation methods
  //-------------------------------------------------------------------------

  def mustacheJsonTest(name: String): Unit = {
    test(name + " JSON") {
      val jText = IOUtil.loadTextFile(new File(rootDir, name + ".js")).trim.stripSuffix(";")
      // lets trim the 'var x = ' part to make valid json
      val idx = jText.indexOf("=")
      val jsonText = if (idx >= 0) jText.substring(idx + 1) else jText
      JSON.parseFull(jsonText) match {
        case Some(json) =>
          debug("Parsed json: " + json)
          
          json match {
            case attributes: Map[String,_] =>
              assertMustacheTest(name, attributes)
            case v =>
              fail("Cannot process JSON type: " + v)
          }
        case _ =>
          fail("Could not parse JSON text - strings maybe need quoting?: " + jsonText)
      }
    }
  }
}