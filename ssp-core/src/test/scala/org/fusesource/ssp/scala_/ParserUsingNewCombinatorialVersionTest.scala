package org.fusesource.ssp.scala_

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
    println("Parsing...")
    println(text)
    println

    val parser = new SspParser()
    parser.parse(text) match {
      case parser.Success(lines : List[PageFragment], _) =>
        for (line <- lines) {
          println(line)
        }
        println
        lines

      case e =>
        fail("Failed to parse! " + e)
        Nil
    }
  }
}