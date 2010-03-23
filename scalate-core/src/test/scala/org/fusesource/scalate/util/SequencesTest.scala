package org.fusesource.scalate.util

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.fusesource.scalate.util.Sequences._

/**
 * @version $Revision: 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class SequencesTest extends FunSuite with Logging {

  test("removeDuplicates works") {
    val list = List("a", "a", "b", "c", "a")

    val unique = removeDuplicates(list)

    expect(List("a", "b", "c")) { unique }

    debug("removing duplicates from " + list + " created " + unique)
  }

}
