package org.fusesource.scalate.console

import java.io.File

/**
 * Represents a template archetype (namely a template used to generate other templates that can then be customized)
 *
 * @version $Revision: 1.1 $
 */
case class Archetype(file: File) {

  def uri = file.getName

  def name = file.getName

  /**
   * Returns the extension of the template archetype
   */
  def extension = {
    val i = uri.lastIndexOf('.')
    if (i > 0) {
      uri.substring(i + 1)
    }
    else {
      uri
    }
  }

  def archetype = file.getPath

  /**
   * Returns the URI to post to that generates the new template for this archetype
   */
  def createUri(newTemplatePrefix: String) = {
    "/scalate/createTemplate?name=" + newTemplatePrefix + "archetype=" + file.getPath 
  }
}