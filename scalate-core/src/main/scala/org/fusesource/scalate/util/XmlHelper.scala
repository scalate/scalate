package org.fusesource.scalate.util

import scala.xml._
import scala.xml.parsing.ConstructingParser
import scala.io.Source

/**
 * @version $Revision: 1.1 $
 */

object XmlHelper extends Logging {

  /**
   * Parsers some markup which might not be a single Xml document
   * by wrapping it in a root XML element first
   */
  def textToNodeSeq(text: String): NodeSeq = {
    fine("parsing markup: " + text)
    
    val src = Source.fromString("<p>" + text + "</p>");
    val cpa = ConstructingParser.fromSource(src, false);
    cpa.document().docElem.child
  }

}