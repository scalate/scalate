package org.fusesource.scalate.ssp

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import collection.mutable.HashMap

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class ParserUsingNewCombinatorialVersionTest extends ParserUsingOriginalParserTest {

  override def assertValid(text: String): List[PageFragment] = {
    log("Parsing...")
    log(text)
    log("")

    val lines = (new SspParser).getPageFragments(text)
    for (line <- lines) {
      log(line)
    }
    log("")
    lines
  }
}