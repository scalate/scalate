package org.fusesource.scalate.support

object CharData {

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