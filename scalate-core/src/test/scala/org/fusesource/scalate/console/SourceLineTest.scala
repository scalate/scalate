package org.fusesource.scalate.console

import org.fusesource.scalate.util.Logging
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SourceLineTest extends FunSuite with Logging {
  val line = SourceLine(1, "abcd")

  test("split line") {
    expect(("", "a", "bcd")) { line.splitOnCharacter(0) }
    expect(("a", "b", "cd")) { line.splitOnCharacter(1) }
    expect(("ab", "c", "d")) { line.splitOnCharacter(2) }
    expect(("abc", "d", "")) { line.splitOnCharacter(3) }
    expect(("abcd", "", "")) { line.splitOnCharacter(4) }
    expect(("abcd", "", "")) { line.splitOnCharacter(5) }
  }

}