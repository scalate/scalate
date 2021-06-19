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
package org.fusesource.scalate.mustache

import java.io.File
import org.fusesource.scalate.util.IOUtil

import org.json4s._
import org.json4s.native.JsonMethods._

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
  implicit val formats: Formats = DefaultFormats

  def mustacheJsonTest(name: String): Unit = {
    test(name + " JSON") {
      val jText = IOUtil.loadTextFile(new File(rootDir, name + ".js")).trim.stripSuffix(";")
      // lets trim the 'var x = ' part to make valid json
      val idx = jText.indexOf("=")
      val jsonText = if (idx >= 0) jText.substring(idx + 1) else jText
      parseOpt(jsonText) match {
        case Some(json) =>
          debug("Parsed json: %s", json)

          json.extract[Map[String, Any]] match {
            case attributes: Map[_, _] =>
              assertMustacheTest(name, attributes.asInstanceOf[Map[String, Any]])
            case v =>
              fail("Cannot process JSON type: " + v)
          }
        case _ =>
          fail("Could not parse JSON text - strings maybe need quoting?: " + jsonText)
      }
    }
  }
}
