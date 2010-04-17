package org.fusesource.scalate.squery.support

import util.parsing.combinator.RegexParsers
import util.parsing.input.{CharSequenceReader, NoPosition, Position}
import org.fusesource.scalate.TemplateException
import org.fusesource.scalate.squery._

class CssScanner extends RegexParsers {
  protected def append(text: String*) = text.mkString("")

  // regex values...
  private val h = """[0-9a-f]"""


  //private val nonasciiRange = """\200-\377]"""
  private val nonasciiRange = """\xC8-\u0179"""
  private val nonascii = append("""[""", nonasciiRange, """]""")
  private val unicode = """(\\""" + h + """{1,6}[ \t\r\n\f]?)"""
  private val escape = append("""(""", unicode, """|\\[ -~""", nonasciiRange, """])""")
  private val nmstart = append("""[a-z]|""", nonascii, """|""", escape)
  private val nmchar = append("""[a-z0-9-]|""", nonascii, """|""", escape)
  private val string1 = append("""\"([\t !#$%&(-~]|\\""", nl, """|\'|""", nonascii, """|""", escape, """)*\""")
  private val string2 = append("""\'([\t !#$%&(-~]|\\""", nl, """|\"|""", nonascii, """|""", escape, """)*\'""")


  private val ident = append("""[-]?""", nmstart, """(""", nmchar, """)*""")
  private val name = append("""(""", nmchar, """)+""")
  private val num = """[0-9]+|[0-9]*"."[0-9]+"""
  private val url = append("""([!#$%&*-~]|""", nonascii, """|""", escape, """)*""")
  private val w = """[ \t\r\n\f]*"""
  private val nl = """\n|\r\n|\r|\f"""
  private val range = append("""\?{1,6}|""", h, """(\?{0,5}|""", h, """(\?{0,4}|""", h, """(\?{0,3}|""", h, """(\?{0,2}|""", h, """(\??|""", h, """)))))""")


  // TODO fix up the regex to be more exact...
  //val IDENT = ident.r
  val IDENT = """[-]?[^\s\d\p{Punct}][^\s\.\#\(\[\"\']*""".r

  def STRING = string1.r | string2.r

  val S = """\s+""".r

  val repS = """\s*""".r
  val rep1S = """\s+""".r

  val COMMA = ","


  val PLUS = """\+""".r
  val GREATER = """>""".r
  val TILDE = """~""".r

  def URI = "url(" ~> S ~> (STRING | url.r) <~ S <~ ")"


  def FUNCTION = IDENT <~ "("

  val INCLUDES = "~="
  val DASHMATCH = "|="
  val PREFIXMATCH = "^="
  val SUFFIXMATCH = "$="
  val SUBSTRINGMATCH = "*="


  val IMPORTANT_SYM = append("""!""", w, """important""").r

  val NUMBER = num.r
  val PERCENTAGE = append(num, """%""").r

  def LENGTH = NUMBER ~ ("px" | "cm" | "mm" | "in" | "pt" | "pc")

  def ANGLE = NUMBER ~ ("deg" | "rad" | "grad")

  def EMS = NUMBER ~ "em"

  def EXS = NUMBER ~ "ex"

  def TIME = NUMBER ~ ("ms" | "s")

  def FREQ = NUMBER ~ ("Hz" | "kHz")

  def DIMENSION = NUMBER ~ IDENT

  def UNICODERANGE = """U\+""" ~ (range.r | append(h, """{1,6}-""", h, """{1,6}""").r)
}

/**
 * Parser of <a href="http://www.w3.org/TR/css3-syntax">CSS3 selectors</a>
 *
 * @version $Revision : 1.1 $
 */
class CssParser extends CssScanner {
  private def phraseOrFail[T](p: Parser[T], in: String): T = {
    var x = phrase(p)(new CharSequenceReader(in))
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
      }
      else {
        Selector(s, cs)
      }
  }

  //  combinator
  //    /* combinators can be surrounded by whitespace */
  //    : PLUS S* | GREATER S* | TILDE S* | S+

  def combinator_simple_selector_sequence = (opt(PLUS | GREATER | TILDE) ~ simple_selector_sequence) ^^ {
    case c ~ s =>
      c match {
        case Some(">") => ChildCombinator(s)
        case Some("+") => AdjacentSiblingdCombinator(s)
        case Some("~") => GeneralSiblingCombinator(s)
        case _ => DescendantCombinator(s)
      }
  }

  //  simple_selector_sequence
  //    : [ type_selector | universal ]
  //      [ HASH | class | attrib | pseudo | negation ]*
  //    | [ HASH | class | attrib | pseudo | negation ]+

  def simple_selector_sequence = simple_selector_sequence_1 | simple_selector_sequence_2

  def simple_selector_sequence_1 = (type_selector | universal) ~ rep(hash | className | attrib | pseudo | negation) ^^ {
    case t ~ l => Selector(t :: l)
  }

  def simple_selector_sequence_2 = rep1(hash | className | attrib | pseudo | negation) ^^ {case l => Selector(l)}

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

  def namespace_prefix = ((IDENT ^^ {NamespacePrefixSelector(_)}) | ("*" ^^ {case _ => AnySelector})) <~ "|"

  //  element_name
  //    : IDENT

  def element_name = (IDENT ^^ {ElementNameSelector(_)})


  //  universal
  //    : [ namespace_prefix ]? '*'

  def universal = (opt(namespace_prefix) <~ "*") ^^ {
    case op => op match {
      case Some(p) => p
      case _ => AnySelector
    }
  }

  //  class
  //    : "." IDENT

  def className = ("." ~> IDENT) ^^ {ClassSelector(_)}

  def hash = ("#" ~> IDENT) ^^ {IdSelector(_)}

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
    case n ~ v =>
      println("got n: " + n + " v " + v)
      // TODO add the value thingy...
      n
  }

  def attribute_name = (opt(namespace_prefix) ~ IDENT <~ repS) ^^ {
    case np ~ i =>
      val attName = AttributeNameSelector(i)
      np match {
        case Some(p) => Selector(p :: attName :: Nil)
        case _ => attName
      }
  }

  def attribute_value = ((PREFIXMATCH | SUFFIXMATCH | SUBSTRINGMATCH | "=" | INCLUDES | DASHMATCH) <~ repS) ~
          ((IDENT | STRING) <~ repS) ^^ {
    case p ~ i =>
      println("got p " + p + " i " + i)
      AnySelector
  }

  //  pseudo
  //    /* '::' starts a pseudo-element, ':' a pseudo-class */
  //    /* Exceptions: :first-line, :first-letter, :before and :after. */
  //    /* Note that pseudo-elements are restricted to one per selector and */
  //    /* occur only in the last simple_selector_sequence. */
  //    : ':' ':'? [ IDENT | functional_pseudo ]

  def pseudo = (":" ~ opt(":")) ~> (pseudo_ident | functional_pseudo)

  def pseudo_ident = IDENT ^^ {Selector.pseudoSelector(_)}

  //  functional_pseudo
  //    : FUNCTION S* expression ')'

  def functional_pseudo = (FUNCTION <~ repS) ~ (expression <~ ")") ^^ {case f ~ e => Selector.pseudoFunction(f)}


  //  expression
  //    /* In CSS3, the expressions are identifiers, strings, */
  //    /* or of the form "an+b" */
  //    : [ [ PLUS | '-' | DIMENSION | NUMBER | STRING | IDENT ] S* ]+

  def expression = rep1(("+" | "-" | DIMENSION | NUMBER | STRING | IDENT) <~ repS)

  //  negation
  //    : NOT S* negation_arg S* ')'
  def negation = (":NOT(" ~ repS) ~> negation_arg <~ (repS ~ ")") ^^ {case a => NotSelector(a)}

  //  negation_arg
  //    : type_selector | universal | HASH | class | attrib | pseudo

  def negation_arg: Parser[Selector] = type_selector | universal | hash | className | attrib | pseudo

}

class InvalidCssSelectorException(val brief: String, val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos)