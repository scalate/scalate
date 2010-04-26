/**
 *  Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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
package org.fusesource.scalate.scaml

import org.fusesource.scalate.RenderContext

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ScamlOptions {

  object Format extends Enumeration {
    val xhtml, html4, html5 = Value
  }


  val DEFAULT_FORMAT = Format.xhtml
  val DEFAULT_AUTOCLOSE = List(
    "meta",
    "img",
    "link",
    "br",
    "hr",
    "input")

  val DEFAULT_INDENT = "  "
  val DEFAULT_NL = "\n"
  val DEFAULT_UGLY = false


  var format = DEFAULT_FORMAT

  var autoclose = DEFAULT_AUTOCLOSE

  /**
   * The indent type used in Scaml markup output.  Defaults
   * to two spaces.
   */
  var indent = DEFAULT_INDENT

  /**
   * The newline separator used in the produced markup output.  Defaults
   * to <code>"\n"</code>.
   */
  var nl = DEFAULT_NL

  /**
   * Use ugly content rendering by default.  When ugly rendering is
   * enabled, evaluated content is not re-indented and newline
   * preservation is not applied either.
   */
  var ugly = DEFAULT_UGLY


}