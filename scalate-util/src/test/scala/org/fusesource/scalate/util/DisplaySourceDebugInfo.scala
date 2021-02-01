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
package org.fusesource.scalate.util

import slogging.StrictLogging

import _root_.java.io.File

/**
 * Displays the source debugging info associated with a class file.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object DisplaySourceDebugInfo extends StrictLogging {

  def main(args: Array[String]) = {
    val fileName = if (args.size > 0) args(0) else "scalate-sample/src/main/webapp/WEB-INF/_scalate/classes/scaml/$_scalate_$missingAttribute_scaml$.class"
    logger.info("Loading class file: " + fileName)

    val file = new File(fileName)
    if (file.exists) {
      logger.info(SourceMapInstaller.load(file))
    } else {
      logger.warn("ERROR: " + file + " does not exist!")
    }
  }

}
