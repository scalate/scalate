package org.fusesource.scalate.util

import _root_.org.fusesource.scalate.FunSuiteSupport
import Sequences._

/**
 * @version $Revision: 1.1 $
 */
class SequencesTest extends FunSuiteSupport {

  test("removeDuplicates works") {
    val list = List("a", "a", "b", "c", "a")

    val unique = removeDuplicates(list)

    expect(List("a", "b", "c")) { unique }

    debug("removing duplicates from " + list + " created " + unique)
  }

}
