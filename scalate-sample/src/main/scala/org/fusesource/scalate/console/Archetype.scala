package org.fusesource.scalate.console

import _root_.java.io.File

/**
 * Represents a template archetype (namely a template used to generate other templates that can then be customized)
 *
 * @version $Revision: 1.1 $
 */
case class Archetype(file: File) {

  /**
   * Returns the extension of the template archetype
   */
  def extension = {
    val i = file.getName.lastIndexOf('.')
    if (i > 0) {
      uri.substring(i + 1)
    }
    else {
      uri
    }
  }
}