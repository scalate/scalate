package org.fusesource.scalate.util

import slogging.LazyLogging

import java.io.File

/**
 * Displays the source debugging info associated with a class file.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object DisplaySourceDebugInfo extends LazyLogging {

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
