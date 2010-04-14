package org.fusesource.scalate.support

import util.parsing.combinator.RegexParsers
import util.parsing.input.CharArrayReader
import CharData._
import Integer._

/**
 * @version $Revision : 1.1 $
 */
trait ScalaParseSupport extends RegexParsers {
  val scalaType = """[a-zA-Z0-9\$_\[\]\.\(\)\#\:\<\>\+\-]+""".r


  val EofCh = CharArrayReader.EofCh

  lazy val tripleQuote: Parser[Unit] = "\"" + "\"" + "\"" ^^^ ()
  lazy val anyChar: Parser[Char] = chrExcept(EofCh)

  def chrExcept(cs: Char*): Parser[Char] = elem("chrExcept", ch => (ch != EofCh) && (cs forall (ch !=)))

  def chrOf(cs: Char*): Parser[Char] = elem("chrOf", ch => (cs exists (ch ==)))

  def chrOf(cs: String): Parser[Char] = chrOf(cs.toArray: _*)


  def takeUntil(cond: Parser[Any]): Parser[String] = takeUntil(cond, anyChar)

  def takeUntil(cond: Parser[Any], p: Parser[Char]): Parser[String] = rep(not(cond) ~> p) ^^ {_.mkString}

  def takeWhile(p: Parser[Char]): Parser[String] = rep(p) ^^ {_.mkString}

  def surround[T](c: Char, p: Parser[T]): Parser[T] = c ~> p <~ c

  def surround[T](delim: Parser[Any], p: Parser[T]): Parser[T] = delim ~> p <~ delim

  def squoted[T](p: Parser[T]): Parser[T] = surround('\'', p)

  def dquoted[T](p: Parser[T]): Parser[T] = surround('"', p)

  def tquoted[T](p: Parser[T]): Parser[T] = surround(tripleQuote, p)

  lazy val octalDigit: Parser[Char] = accept("octalDigit", isOctalDigit)
  lazy val hexDigit: Parser[Char] = accept("hexDigit", isHexDigit)


  override def accept[U](expected: String, f: PartialFunction[Elem, U]): Parser[U] =
    ('\\' ~> 'u' ~> uniEscapeSeq ^? f) | super.accept(expected, f)

  private lazy val printableChar: Parser[Char] = elem("printable", !isControl(_))
  private lazy val printableChars: Parser[String] = takeWhile(printableChar)
  private lazy val printableCharNoDoubleQuote: Parser[Char] = elem("nodq", ch => !isControl(ch) && ch != '"')

  private lazy val multiLineCharGroup: Parser[String] =
  opt(elem('"')) ~ opt(elem('"')) ~ chrExcept('"') ^^ {
    case a ~ b ~ c => List(a, b, Some(c)).flatMap(x => x).mkString
  }

  lazy val charEscapeSeq: Parser[Char] = '\\' ~> (
          (accept("escape", simpleEscape))
                  | (octalEscapeSeq)
                  | ('u' ~> uniEscapeSeq)
          )
  lazy val uniEscapeSeq: Parser[Char] = (repN(4, hexDigit)) ^^ (x => parseInt(x.mkString, 16).toChar)

  lazy val octalEscapeSeq: Parser[Char] = octalDigit ~ opt(octalDigit) ~ opt(octalDigit) ^^ {
    case x ~ y ~ z => val digits = x :: List(y, z).flatMap(x => x); parseInt(digits.mkString, 8).toChar // 0377
  }


  lazy val characterLiteral: Parser[String] = squoted(charEscapeSeq | printableChar) ^^ {case x => "'" + x + "'"}

  lazy val stringLiteral: Parser[String] = (tquoted(multiLineChars) ^^ {case x => "\"\"\"" + x + "\"\"\""}) |
          (dquoted(doubleQuotedChars) ^^ {case x => "\"" + x + "\"" })

  lazy val doubleQuotedChars: Parser[String] = takeWhile(charEscapeSeq | printableCharNoDoubleQuote)
  lazy val multiLineChars: Parser[String] = takeUntil(tripleQuote)
}

object CharData {
  import Character._

  val simpleEscape: PartialFunction[Char, Char] = {
    case 'b' => '\b'
    case 't' => '\t'
    case 'n' => '\n'
    case 'f' => '\f'
    case 'r' => '\r'
    case '\"' => '\"'
    case '\'' => '\''
    case '\\' => '\\'
  }

  val zeroDigit: PartialFunction[Char, Char] = {
    case '0' => '0'
  }
  val isNonZeroDigit: PartialFunction[Char, Char] = {
    case '1' => '1'
    case '2' => '2'
    case '3' => '3'
    case '4' => '4'
    case '5' => '5'
    case '6' => '6'
    case '7' => '7'
    case '8' => '8'
    case '9' => '9'
  }
  val isDigit: PartialFunction[Char, Char] = zeroDigit orElse isNonZeroDigit
  val isOctalDigit: PartialFunction[Char, Char] = {
    case '0' => '0'
    case '1' => '1'
    case '2' => '2'
    case '3' => '3'
    case '4' => '4'
    case '5' => '5'
    case '6' => '6'
    case '7' => '7'
  }
  val isHexDigit: PartialFunction[Char, Char] = isDigit orElse {
    case 'a' => 'a'
    case 'b' => 'b'
    case 'c' => 'c'
    case 'd' => 'd'
    case 'e' => 'e'
    case 'f' => 'f'
    case 'A' => 'A'
    case 'B' => 'B'
    case 'C' => 'C'
    case 'D' => 'D'
    case 'E' => 'E'
    case 'F' => 'F'
  }

  def isControl(c: Char) = Character.isISOControl(c)

  def isControl(codepoint: Int) = Character.isISOControl(codepoint)
}