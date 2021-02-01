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
package org.fusesource.scalate.wikitext

import org.fusesource.scalate.util.Files

import java.io.File

/**
 * Helper class for working with wiki pages
 */
object Pages {

  /**
   * Returns the page title of the given wiki file.
   *
   * By default lets use a naming convention mapping to convert "-" to " " and
   * upper case each word.
   *
   * We may wish to use a more sophisticated mechanism such as by loading a
   * file called Files.dropExtension(file) + ".page".
   *
   * See: http://scalate.assembla.com/spaces/scalate/support/tickets/160
   */
  def title(f: File): String = {
    Files.dropExtension(f).replace('-', ' ').split("\\s+").map(_.capitalize).mkString(" ")
  }
}
