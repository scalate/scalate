package org.fusesource.scalate.scuery.support

import xml.Node

/**
 * Represents a matcher on a set of nodes, typically used on attributes in CSS3 selectors
 *
 * @version $Revision: 1.1 $
 */
trait Matcher{
  def matches(nodes: Seq[Node]): Boolean
}

abstract class TextMatcher extends Matcher {
  def matches(nodes: Seq[Node]): Boolean = {
    val text = nodes.mkString(" ")
    matches(text)
  }

  def matches(text: String): Boolean
}

case class EqualsMatch(expected: String) extends TextMatcher {
  def matches(text: String) = text == expected
}

case class PrefixMatch(prefix: String) extends TextMatcher {
  def matches(text: String) = text.startsWith(prefix)
}

case class SuffixMatch(suffix: String) extends TextMatcher {
  def matches(text: String) = text.endsWith(suffix)
}
case class SubstringMatch(substring: String) extends TextMatcher {
  def matches(text: String) = text.contains(substring)
}

/**
 * Matches a whole word after splitting up the value by whitespace
 */
case class IncludesMatch(word: String) extends TextMatcher {
  def matches(text: String) = text.split("\\s").contains(word)
}
/**
 * Matches text starting with the given value or with
 * value immediately followed by "-" (U+002D)
 */
case class DashMatch(value: String) extends TextMatcher {
  private val valueWithDash = value + "-"

  def matches(text: String) = text.startsWith(value) || text.startsWith(valueWithDash)
}

object MatchesAny extends TextMatcher {
  def matches(text: String) = true
}