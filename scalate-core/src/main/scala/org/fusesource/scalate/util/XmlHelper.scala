package org.fusesource.scalate.util

import scala.xml._
import scala.xml.parsing.ConstructingParser
import scala.io.Source

/**
 * @version $Revision : 1.1 $
 */

object XmlHelper extends Logging {

  /**
   * Parsers some markup which might not be a single Xml document
   * by wrapping it in a root XML element first
   */
  def textToNodeSeq(text: String): NodeSeq = {
    debug("parsing markup: " + text)

    val src = Source.fromString("<p>" + text + "</p>");

    // lets deal with HTML entities
    // lets preserve whitespace for <pre> stuff to avoid trimming indentation with code
    object parser extends ConstructingParser(src, true /* keep ws*/ ) {
      override def replacementText(entityName: String): io.Source = {
        entityName match {
          ///case "nbsp" => io.Source.fromString("\u0160");
          case "nbsp" => io.Source.fromString("<![CDATA[&nbsp;]]>");
          case _ => super.replacementText(entityName);
        }
      }
      nextch; // !!important, to initialize the parser
    }
    parser.document().docElem.child
    /*
        val cpa = ConstructingParser.fromSource(src, false);
        cpa.document().docElem.child
    */
  }

}