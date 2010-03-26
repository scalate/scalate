package org.fusesource.scalate.console

import _root_.org.fusesource.scalate.FunSuiteSupport

class SourceLineTest extends FunSuiteSupport {
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