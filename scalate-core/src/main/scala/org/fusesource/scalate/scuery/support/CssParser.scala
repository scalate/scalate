/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.scuery.support

import util.parsing.combinator.RegexParsers
import util.parsing.input.{ CharSequenceReader, NoPosition, Position }
import org.fusesource.scalate.TemplateException
import org.fusesource.scalate.scuery._

class CssScanner extends RegexParsers {
  override def skipWhitespace = false

  //   ident     [-]?{nmstart}{nmchar}*
  def IDENT = (opt("-") ~ nmstart ~ rep(nmchar)) ^^ { case p ~ n ~ l => p.mkString("") + n + l.mkString("") }

  // name      {nmchar}+
  private def name = rep1(nmchar)

  // nmstart   [_a-z]|{nonascii}|{escape}
  private def nmstart = """[_a-zA-Z]""".r | nonascii | escape

  // nonascii  [^\0-\177]
  private def nonascii = """[^\x00-\xB1]""".r

  // unicode   \\[0-9a-f]{1,6}(\r\n|[ \n\r\t\f])?
  private def unicode = """\\[0-9a-fA-F]{1,6}(\r\n|[ \n\r\t\f])?""".r

  // escape    {unicode}|\\[^\n\r\f0-9a-f]
  private def escape = unicode | """\\[^\n\r\f0-9a-fA-F]""".r

  // nmchar    [_a-z0-9-]|{nonascii}|{escape}
  private def nmchar = """[_a-zA-Z0-9-]""".r | nonascii | escape

  // num       [0-9]+|[0-9]*\.[0-9]+
  private[this] val num = """[0-9]+|[0-9]*"."[0-9]+"""

  // string    {string1}|{string2}
  def STRING = string1 | string2

  // string1   \"([^\n\r\f\\"]|\\{nl}|{nonascii}|{escape})*\"
  private[this] val string1 = ("\"" ~> rep("""[^\n\r\f\\"]""".r | ("\\" + nl).r | nonascii | escape) <~ "\"") ^^ { case l => l.mkString("") }

  // string2   \'([^\n\r\f\\']|\\{nl}|{nonascii}|{escape})*\'
  private[this] val string2 = ("'" ~> rep("""[^\n\r\f\']""".r | ("\\" + nl).r | nonascii | escape) <~ "'") ^^ { case l => l.mkString("") }

  // nl        \n|\r\n|\r|\f
  private[this] val nl = """\n|\r\n|\r|\f"""

  // invalid   {invalid1}|{invalid2}
  // invalid1  \"([^\n\r\f\\"]|\\{nl}|{nonascii}|{escape})*
  // invalid2  \'([^\n\r\f\\']|\\{nl}|{nonascii}|{escape})*
  // w         [ \t\r\n\f]*

  val S = """\s+""".r
  val repS = """[\s]*""".r
  val rep1S = """[\s]+""".r

  val COMMA = ","
  val PLUS = """\+""".r
  val GREATER = """>""".r
  val TILDE = """~""".r

  val INCLUDES = "~="
  val DASHMATCH = "|="
  val PREFIXMATCH = "^="
  val SUFFIXMATCH = "$="
  val SUBSTRINGMATCH = "*="

  val NUMBER = num.r
  val INTEGER = """[0-9]""".r

  def DIMENSION = NUMBER ~ IDENT

  // D         d|\\0{0,4}(44|64)(\r\n|[ \t\r\n\f])?
  def D = """d|\\0{0,4}(44|64)(\r\n|[ \t\r\n\f])?""".r

  // E         e|\\0{0,4}(45|65)(\r\n|[ \t\r\n\f])?
  def E = """e|\\0{0,4}(45|65)(\r\n|[ \t\r\n\f])?""".r

  // N         n|\\0{0,4}(4e|6e)(\r\n|[ \t\r\n\f])?|\\n
  def N = """n|\\0{0,4}(4e|6e)(\r\n|[ \t\r\n\f])?|\\n""".r

  // O         o|\\0{0,4}(4f|6f)(\r\n|[ \t\r\n\f])?|\\o
  def O = """o|\\0{0,4}(4f|6f)(\r\n|[ \t\r\n\f])?|\\o""".r

  // T         t|\\0{0,4}(54|74)(\r\n|[ \t\r\n\f])?|\\t
  def T = """t|\\0{0,4}(54|74)(\r\n|[ \t\r\n\f])?|\\t""".r

  //  V         v|\\0{0,4}(58|78)(\r\n|[ \t\r\n\f])?|\\v
  def V = """v|\\0{0,4}(58|78)(\r\n|[ \t\r\n\f])?|\\v""".r
}

/**
 * Parser of <a href="http://www.w3.org/TR/css3-syntax">CSS3 selectors</a>
 *
 * @version $Revision : 1.1 $
 */
class CssParser extends CssScanner {
  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    val x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidCssSelectorException(message, next.pos);
    }
  }

  def parseSelector(in: String) = {
    phraseOrFail(selector, in)
  }

  //  selectors_group
  //    : selector [ COMMA S* selector ]*

  def selectors_group = selector ~ rep((COMMA ~ repS) ~> selector)

  //  selector
  //    : simple_selector_sequence [ combinator simple_selector_sequence ]*

  def selector = (simple_selector_sequence ~ rep(combinator_simple_selector_sequence)) ^^ {
    case s ~ cs =>
      if (cs.isEmpty) {
        s
      } else {
        Selector(s, cs)
      }
  }

  //  combinator
  //    /* combinators can be surrounded by whitespace */
  //    : PLUS S* | GREATER S* | TILDE S* | S+

  def combinator_simple_selector_sequence = (((repS ~> (PLUS | GREATER | TILDE) <~ repS) | rep1S) ~ simple_selector_sequence) ^^ {
    case c ~ s =>
      c match {
        case ">" => ChildCombinator(s)
        case "+" => AdjacentSiblingdCombinator(s)
        case "~" => GeneralSiblingCombinator(s)
        case _ => DescendantCombinator(s)
      }
  }

  //  simple_selector_sequence
  //    : [ type_selector | universal ]
  //      [ HASH | class | attrib | pseudo | negation ]*
  //    | [ HASH | class | attrib | pseudo | negation ]+

  def simple_selector_sequence = simple_selector_sequence_1 | simple_selector_sequence_2

  def simple_selector_sequence_1 = (type_selector | universal) ~ rep(hash | className | attrib | negation | pseudo) ^^ {
    case t ~ l => Selector(t :: l)
  }

  def simple_selector_sequence_2 = rep1(hash | className | attrib | negation | pseudo) ^^ { case l => Selector(l) }

  //  type_selector
  //    : [ namespace_prefix ]? element_name

  def type_selector = (opt(namespace_prefix) ~ element_name) ^^ {
    case on ~ e => on match {
      case Some(n) => Selector(n :: e :: Nil)
      case _ => e
    }
  }

  //  namespace_prefix
  //    : [ IDENT | '*' ]? '|'

  def namespace_prefix = ((opt(IDENT | "*")) <~ "|") ^^ {
    case o => o match {
      case Some("*") => AnySelector
      case Some(prefix) => NamespacePrefixSelector(prefix)
      case _ => NoNamespaceSelector
    }
  }

  //  element_name
  //    : IDENT

  def element_name = (IDENT ^^ { ElementNameSelector(_) })

  //  universal
  //    : [ namespace_prefix ]? '*'

  def universal = (opt(namespace_prefix) <~ "*") ^^ {
    case op => op match {
      case Some(p) => p
      case _ => AnyElementSelector
    }
  }

  //  class
  //    : "." IDENT

  def className = ("." ~> IDENT) ^^ { ClassSelector(_) }

  def hash = ("#" ~> IDENT) ^^ { IdSelector(_) }

  //  attrib
  //    : '[' S* [ namespace_prefix ]? IDENT S*
  //          [ [ PREFIXMATCH |
  //              SUFFIXMATCH |
  //              SUBSTRINGMATCH |
  //              '=' |
  //              INCLUDES |
  //              DASHMATCH ] S* [ IDENT | STRING ] S*
  //          ]? ']'

  def attrib = (("[" ~ repS) ~> attribute_name ~ opt(attribute_value) <~ "]") ^^ {
    case np ~ i ~ v =>
      val matcher = v match {
        case Some(v) => v
        case _ => MatchesAny
      }
      np match {
        case Some(p) => p match {
          case p: NamespacePrefixSelector => NamespacedAttributeNameSelector(i, p.prefix, matcher)
          case _ => AttributeNameSelector(i, matcher)
        }
        case _ => AttributeNameSelector(i, matcher)
      }
  }

  def attribute_name = opt(namespace_prefix) ~ IDENT <~ repS

  def attribute_value = ((PREFIXMATCH | SUFFIXMATCH | SUBSTRINGMATCH | "=" | INCLUDES | DASHMATCH) <~ repS) ~
    ((IDENT | STRING) <~ repS) ^^ {
      case p ~ i =>
        p match {
          case PREFIXMATCH => PrefixMatch(i)
          case SUFFIXMATCH => SuffixMatch(i)
          case SUBSTRINGMATCH => SubstringMatch(i)
          case "=" => EqualsMatch(i)
          case INCLUDES => IncludesMatch(i)
          case DASHMATCH => DashMatch(i)
        }
    }

  //  pseudo
  //    /* '::' starts a pseudo-element, ':' a pseudo-class */
  //    /* Exceptions: :first-line, :first-letter, :before and :after. */
  //    /* Note that pseudo-elements are restricted to one per selector and */
  //    /* occur only in the last simple_selector_sequence. */
  //    : ':' ':'? [ IDENT | functional_pseudo ]

  def pseudo = (":" ~ opt(":")) ~> (functional_nth_pseudo | functional_pseudo | pseudo_ident)

  def pseudo_ident = IDENT ^^ { Selector.pseudoSelector(_) }

  //  functional_pseudo
  //    : FUNCTION S* expression ')'

  def functional_pseudo = (IDENT <~ ("(" ~ repS)) ~ (expression <~ ")") ^^ { case f ~ e => Selector.pseudoFunction(f) }
  def functional_nth_pseudo = ("nth-" ~> IDENT <~ ("(" ~ repS)) ~ (repS ~> nth <~ ")") ^^ { case f ~ e => Selector.pseudoFunction("nth-" + f, e) }

  //  expression
  //    /* In CSS3, the expressions are identifiers, strings, */
  //    /* or of the form "an+b" */
  //    : [ [ PLUS | '-' | DIMENSION | NUMBER | STRING | IDENT ] S* ]+

  def expression = rep1(("+" | "-" | DIMENSION | STRING | IDENT) <~ repS)

  // nth
  //  : S* [ ['-'|'+']? INTEGER? {N} [ S* ['-'|'+'] S* INTEGER ]? |
  //         ['-'|'+']? INTEGER | {O}{D}{D} | {E}{V}{E}{N} ] S*
  def nth = (opt("-" | "+") ~ (opt(integer) <~ N) ~ opt((repS ~> ("-" | "+") <~ repS) ~ integer)) ^^ {
    case os ~ on ~ on2 =>
      val a = on match {
        case Some(n) => if (os == Some("-")) { n * -1 } else { n }
        case _ => 0
      }
      val b = on2 match {
        case Some(s ~ i) =>
          if (s == "-") { i * -1 } else { i }
        case _ => 0
      }
      NthCounter(a, b)
  } | (opt("-" | "+") ~ integer) ^^ {
    case os ~ i =>
      val b = if (os == Some("-")) { i * -1 } else { i }
      NthCounter(0, b)
  } | (O ~ D ~ D) ^^ {
    case _ => OddCounter
  } | (E ~ V ~ E ~ N) ^^ {
    case _ => EvenCounter
  }

  def integer = INTEGER ^^ { Integer.parseInt(_) }

  //  negation
  //    : NOT S* negation_arg S* ')'
  def negation = (":" ~ N ~ O ~ T ~ "(" ~ repS) ~> negation_arg <~ (repS ~ ")") ^^ { case a => NotSelector(a) }

  //  negation_arg
  //    : type_selector | universal | HASH | class | attrib | pseudo

  def negation_arg: Parser[Selector] = type_selector | universal | hash | className | attrib | pseudo

}

class InvalidCssSelectorException(val brief: String, val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos)
