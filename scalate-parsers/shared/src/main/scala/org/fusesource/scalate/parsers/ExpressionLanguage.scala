package org.fusesource.scalate.parsers

import scala.util.matching.Regex.Match

object ExpressionLanguage {
  protected val operators = Map("eq" -> "==", "ne" -> "!=",
    "gt" -> ">", "ge" -> ">=",
    "lt" -> "<", "le" -> "<=",
    "not" -> "!")

  protected val notEmptyRegex = """(\s|^)(not\s+empty)\s(.+)""".r
  protected val emptyRegex = """(\s|^)(empty)\s(.+)""".r
  protected val lengthRegex = """fn:length\((.+)\)""".r

  def asScala(el: String): String = {
    // lets switch the EL style indexing to Scala parens and switch single quotes to doubles
    var text = el.replace('[', '(').
      replace(']', ')').
      replace('\'', '\"')

    def space(m: Match): String = if (m.start == 0) "" else " "

    // "not empty xxx" => "!(xxx isEmpty)"
    text = notEmptyRegex.replaceAllIn(text, { m => space(m) + "!(" + m.subgroups.last + " isEmpty)" })

    // "empty whatever" => "whatever isEmpty"
    text = emptyRegex.replaceAllIn(text, { m => space(m) + m.subgroups.last + " isEmpty" })

    // replace EL operators
    for ((a, b) <- operators) {
      text = text.replaceAll("(\\s|^)" + a + "\\s", " " + b + " ")
    }

    // foo.bar => foo.getBar
    var first = true
    text = text.split('.').map(s =>
      if (!first && s.length > 0 && s(0).isUnicodeIdentifierStart) {
        "get" + s.capitalize
      } else {
        first = false
        s
      }).mkString(".")

    // fn:length(foo) => foo.size
    text = lengthRegex.replaceAllIn(text, { m => m.subgroups.last + ".size" })

    text
  }
}
