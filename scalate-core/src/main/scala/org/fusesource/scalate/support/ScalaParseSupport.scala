package org.fusesource.scalate.support

/**
 * @version $Revision: 1.1 $
 */

trait ScalaParseSupport {

  val scalaType = """[a-zA-Z0-9\$_\[\]\.\(\)\#\:\<\>\+\-]+""".r
}