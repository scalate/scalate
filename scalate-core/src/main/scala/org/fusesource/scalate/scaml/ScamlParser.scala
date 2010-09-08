/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package org.fusesource.scalate.scaml

import annotation.tailrec
import _root_.org.fusesource.scalate.{InvalidSyntaxException}
import scala.util.parsing.combinator._
import util.parsing.input.{Positional, CharSequenceReader}
import scala.None
import collection.mutable.ListBuffer
import java.util.regex.Pattern
import java.io.File
import org.fusesource.scalate.util.IOUtil
import org.fusesource.scalate.support.{Text, ScalaParseSupport}

/**
 * Base class for parsers which use indentation to define
 * structure.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class IndentedParser extends RegexParsers() {

  var skipWhitespaceOn = false
  override def skipWhitespace = skipWhitespaceOn

  def skip_whitespace[T](p: => Parser[T], enable:Boolean=true): Parser[T] = Parser[T] { in =>
    val was = skipWhitespaceOn
    skipWhitespaceOn = enable
    val result = p(in)
    skipWhitespaceOn = was
    result
  }

  var mismatch_indent_desc:String = null
  var mismatch_indent:Parser[Any] = null

  var indent_desc:String = null
  var indent_unit:Parser[Any] = null
  var indent_level:Int = 1


  /** A parser generator for a specified range of repetitions.
   *
   * <p> repRange(min, max, p) uses `p' from `min' upto `max' times to parse the input 
   *       (the result is a `List' of the consecutive results of `p')</p>
   *
   * @param p a `Parser' that is to be applied successively to the input
   * @param n the exact number of times `p' must succeed
   * @return A parser that returns a list of results produced by repeatedly applying `p' to the input
   *        (and that only succeeds if `p' matches exactly `n' times).
   */
  def repRange[T](min: Int, max: Int, p: => Parser[T]): Parser[List[T]] =
    if (max == 0) success(Nil) else Parser { in =>
      val elems = new ListBuffer[T]
      @tailrec def applyp(in0: Input): ParseResult[List[T]] = {
        if (elems.length == max) Success(elems.toList, in0)
        else p(in0) match {
          case Success(x, rest)   => elems += x ; applyp(rest)
          case ns: NoSuccess      => if (elems.length < min) {ns} else {Success(elems.toList, in0)}
        }
      }
      applyp(in)
    }


  def current_indent(strict:Boolean=false): Parser[Any] = {
    if( indent_level==0 ) {
      success()
    } else if( indent_unit!=null ) {

      // Look for mismatch indent types..
      var rc: Parser[Any] = mismatch_indent ~ err("Inconsistent indent detected: indented with "+mismatch_indent_desc+" but previous lines were indented with "+indent_desc)
      if( strict ) {
        // Look for indents that are too deep.
        rc |= repN(indent_level,indent_unit)~"""[ \t]+""".r~err("Inconsistent indent level detected: intended too deep")
        // eat empty lines..
        rc |= rep(indent_unit) ~ """\r?\n""".r ~> current_indent(strict)
      }

      // this is the normal indent case
      rc |= repN(indent_level,indent_unit)

      if( !strict ) {
        // preseve emtpy lines inside filters..
        rc |= repRange(0, indent_level-1, indent_unit) ~ guard("""\r?\n""".r ~ current_indent(strict))
      }

      if( indent_level > 0 ) {
        // Look for indents that are too shallow
        rc |= repN(indent_level-1,indent_unit)~"""[ \t]+""".r~err("Inconsistent indent level detected: intended too shallow")
      }

      rc | failure("Inconsistent indent detected: "+indent_level+" indent level(s) were expected");
      
    } else {
      """ +""".r ^^ {
        case s=>
        indent_desc  = "spaces"
        mismatch_indent_desc = "tabs"
        indent_unit=failure("expected space based indent") | s 
        mismatch_indent = """\t+""".r
      } |
      """\t+""".r ^^ {
        case s=>
        indent_desc  = "tabs"
        mismatch_indent_desc = "spaces"
        indent_unit= failure("expected tab based indent") | s
        mismatch_indent = """ +""".r
      } 
    }
  }

  def indent[U](p:Parser[U], strict:Boolean=false) =
    ( current_indent(strict) ^^ { s=>{ indent_level+=1;s}  } ) ~> p ^^ { s=>{indent_level-=1;  s}}

}

object Trim extends Enumeration {
  val Outer, Inner, Both = Value
}

sealed trait Statement extends Positional
sealed trait TextExpression extends Statement

case class Newline(skip:Boolean=true) extends Statement
case class EvaluatedText(code:Text, body:List[Statement], preserve:Boolean, sanitize:Option[Boolean], ugly:Boolean) extends TextExpression
case class LiteralText(text:List[Text], sanitize:Option[Boolean]) extends TextExpression {
  // every odd item starting at 1 is an interpolated expression like #{foo} 
  def isStatic = text.size < 2
}
case class Element(tag:Option[Text], attributes:List[(Any,Any)], text:Option[TextExpression], body:List[Statement], trim:Option[Trim.Value], close:Boolean) extends Statement
case class ScamlComment(text:Option[Text], body:List[Text]) extends Statement
case class HtmlComment(conditional:Option[Text], text:Option[Text], body:List[Statement]) extends Statement
case class Executed(code:List[Text], body:List[Statement]) extends Statement
case class FilterStatement(flags:List[Text], filters:List[Text], body:List[Text]) extends Statement
case class Attribute(kind:Text, name: Text, className: Text, defaultValue: Option[Text], autoImport:Boolean) extends Statement
case class Doctype(line:List[Text]) extends Statement

/**
 * Parses a HAML/Scala based document.  Original inspired by the ruby version at http://haml-lang.com/
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ScamlParser extends IndentedParser() with ScalaParseSupport  {

  override def upto[T](p1: Parser[T]): Parser[Text] = {
    text(
      text("""\z""".r) ~ failure("end of file") ^^{ null } |
      guard(p1) ^^ { _ => "" } |
      rep1(not(p1) ~> ".".r) ^^ { _.mkString("") }
    )
  }

  val dot                     = text(""".+""".r)
  val opt_dot                 = text(""".*""".r)
  val space                   = text("""[ \t]+""".r)
  val opt_space               = text("""[ \t]*""".r)

  val nl                      = text("""\r?\n""".r)
  val any_space_then_nl       = text("""[ \t]*\r?\n""".r)
  val xml_ident               = text("""[a-zA-Z_0-9][\:a-zA-Z_0-9-]*""".r)
  val tag_ident               = xml_ident | "'" ~> upto("'") <~ "'"
  val css_name                = text("""[a-zA-Z_0-9][a-zA-Z_0-9-]*""".r)


  val scala_ident             = text("""[a-zA-Z_]\w*""".r)
  val qualified_type          = text(scalaType)

  def eval_string_escapes(value:Text) = {
    value.replaceAll(Pattern.quote("\\b"),"\b").
          replaceAll(Pattern.quote("\\f"),"\f").
          replaceAll(Pattern.quote("\\n"),"\n").
          replaceAll(Pattern.quote("\\r"),"\r").
          replaceAll(Pattern.quote("\\t"),"\t")
  }

  val scala_string_literal    = text("\""~>"""([^"\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*|[ \t]*""".r<~"\"")
  val ruby_string_literal     = text("'"~>"""([^'\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*|[ \t]*""".r<~"'")
  val string_literal          = scala_string_literal | ruby_string_literal

  val whole_number            = text("""-?\d+""".r)
  val decimal_number          = text("""(\d+(\.\d*)?|\d*\.\d+)""".r)
  val floating_point_number   = text("""-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r)
  def symbol = ":"~>scala_ident

  // Haml hash style attributes are any valid ruby hash expression. The scala version should
  // either accept the same to allow easy migration of existing haml pages, or accept a
  // valid scala Map expression.
  //
  def hash_style_attributes = prefixed("{", skip_whitespace( repsep(hash_attribute_entry, ","))) <~ opt_space ~ "}"
  def hash_attribute_entry: Parser[(Any, Any)] =
    expression ~ ("=>" ~> expression) ^^ { case key~value => (key, value) }

  def expression: Parser[Any] =
    (
      string_literal |
      whole_number |
      decimal_number |
      floating_point_number |
      symbol
    ) ^^ { s=>eval_string_escapes(s) } |
    ( xml_ident | "{" ~> skip_whitespace(upto("}"),false) <~ "}" ) ^^ {
      x=>EvaluatedText(x, List(), true, Some(true), false)
    }

  def html_style_attributes = prefixed("(", skip_whitespace(rep(html_attribute_entry))) <~ opt_space~")"
  def html_attribute_entry: Parser[(Any, Any)] =
    xml_ident ~ ("=" ~> string_literal) ^^ {
      case key~value =>
        (key, parse(skip_whitespace(literal_text(Some(true)), false), value.value))
    } |
    (
      xml_ident ~ ("=" ~> xml_ident) |
      xml_ident ~ ("=" ~"{" ~> skip_whitespace(upto("}"),false) <~ "}" )
    ) ^^ {
      case key~value =>
        (key, EvaluatedText(value, List(), true, Some(true), false))
    } |
    xml_ident ^^ {
      case key =>
        (key, EvaluatedText(Text("true").setPos(key.pos), List(), true, Some(true), false))
    }


  def class_entry:Parser[(Any, Any)] = "." ~> css_name ^^ { case x=> ("class", x) }
  def id_entry:Parser[(Any, Any)] = "#" ~> css_name ^^ { case x=> ("id", x) }

  def attributes =
          (rep(class_entry|id_entry)) ~
          (rep(hash_style_attributes|html_style_attributes)^^ {x=>x.flatMap{y=>y}}) ^^
          { case l1~l2 => l1:::l2 }

  def trim: Parser[Trim.Value] =
      "><" ^^{ s=> Trim.Both }  |
      "<>" ^^{ s=> Trim.Both }  |
      ">"  ^^{ s=> Trim.Outer } |
      "<"  ^^{ s=> Trim.Inner } 

  def element_text:Parser[Option[TextExpression]] = 
    prefixed("=", upto(nl) <~ nl) ^^ { x=> Some(EvaluatedText(x, List(), false, None, false)) } |
    opt_space ~ nl ^^ { x=>None } |
    space ~> literal_text(None) <~ any_space_then_nl ^^ { x=>Some(x) }


  def full_element_statement:Parser[Element] =
    opt("%"~>tag_ident) ~ attributes ~ opt(trim)  <~ ( "/" ~! opt_space ~ nl ) ^^ {
      case (tag~attributes~wsc) => Element(tag, attributes, None, List(), wsc, true)
    } |
    opt("%"~>tag_ident) ~ attributes ~ opt(trim) ~ element_text ~ statement_block ^^ {
        case ((tag~attributes~wsc~text)~body) => Element(tag, attributes, text, body, wsc, false)
    }

  def element_statement:Parser[Element] = guarded("%"|"."|"#"~css_name, full_element_statement)

  def haml_comment_statement = prefixed("-#", opt(dot)<~nl) ~ rep(indent(opt_dot<~nl)) ^^ { case text~body=> ScamlComment(text,body) }
  def html_comment_statement = prefixed("/", opt(prefixed("[", upto("]") <~"]")) ~ opt(dot)<~nl ) ~ statement_block ^^ { case conditional~text~body=> HtmlComment(conditional,text,body) }

  def evaluated_fragment:Parser[List[Text]]  = wrapped("#{", "}") ~ opt(litteral_fragment) ^^ {
    case code~Some(text)=>{ code :: text }
    case code~None=>{ code :: Nil }
  }

  val litteral_part:Parser[Text] =
    upto("#{" | """\#{""" | """\\#{"""|any_space_then_nl) ~
      opt(
        """\#{""" ~ opt(litteral_part) ^^ { case x~y=> "#{"+y.getOrElse(Text("")) }  |
        """\\""" ^^ { s=>"""\""" }
      ) ^^ {
        case x~Some(y) => x+y
        case x~None => x
      }

  val litteral_fragment:Parser[List[Text]] = opt(litteral_part) ~ opt(evaluated_fragment) ^^ {
    case None~Some(code)=>{ Text("") :: code }
    case None~None=>{ Text("") :: Nil }
    case Some(text)~Some(code)=>{ text :: code }
    case Some(text)~None=>{ text :: Nil }
  }

  def literal_text(sanitize:Option[Boolean]) = litteral_fragment ^^ { LiteralText(_, sanitize) }

  def text_statement = (
          prefixed("""\""", literal_text(None))      |
          prefixed("&=="~opt_space, literal_text(Some(true)) )  |
          prefixed("!=="~opt_space, literal_text(Some(false)) ) |
          prefixed("&"~space, literal_text(Some(true)) )  |
          prefixed("!"~space, literal_text(Some(false)) ) |
          literal_text(None)
        ) <~ any_space_then_nl

  def evaluated_statement = (opt("&"|"!")~("="|"~~"|"~")) ~! (( upto(nl) <~ nl ) ~ statement_block) ^^ {
      case (sanitize~preserve)~(code~body) => EvaluatedText(code, body, "="!=preserve, sanitize match {
        case Some("&") => Some(true)
        case Some("!") => Some(false)
        case None => None
      }, "~~"==preserve)
    }

  val attribute = skip_whitespace( opt(text("import")) ~ text("var"|"val") ~ scala_ident ~ (":" ~> qualified_type) ) ~ ( opt_space ~> opt( "="~opt_space ~> upto(nl) ) ) ^^ {
    case (p_import~p_kind~p_name~p_type)~p_default => Attribute(p_kind, p_name, p_type, p_default, p_import.isDefined)
  }

  def attribute_statement = prefixed("-@", attribute <~ nl) 

  def executed_statement =
    prefixed("-" ~ opt_space ~ nl,  rep1(indent(opt_dot<~nl))) ^^ {
      case code=> Executed(code,List())
    } |
    prefixed("-" ~ opt_space, dot <~ nl) ~ statement_block ^^ {
      case code~body=> Executed(code::Nil,body)
    } 

  def filter_statement = prefixed(":",
      ( rep(text("~" | "!" | "&") ) ~ rep1sep(text("""[^: \t\r\n]+""".r), """[ \t]*:[ \t]*""".r ) )<~ nl
    ) ~ rep(indent(opt_dot<~nl)) ^^ {
      case (flags~code)~body=>
        FilterStatement(flags, code,body) 
  }

  def doctype_statement = prefixed("!!!", rep(opt_space ~> text("""[^ \t \r \n]+""".r)) <~ opt_space ~ nl) ^^ { Doctype(_) }

  def statement:Parser[Statement] = positioned(
      haml_comment_statement |
      html_comment_statement |
      evaluated_statement |
      element_statement |
      attribute_statement |
      executed_statement |
      doctype_statement |
      filter_statement |
      text_statement
    )


  def statement_block = rep(indent(statement, true))

  def parser = rep(
    space ~ err("Inconsistent indent level detected: intended too shallow") ^^ { null } |
    nl ^^ { x=> Newline() } |
    statement
  ) ^^ { case x=> x.filter(_ != Newline()) } 

  def parse(in:String) = {
    var content = in;
    if( !in.endsWith("\n") ) {
      content = in + "\n"
    }
    val x = phrase(parser)(new CharSequenceReader(content))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

  def parse[T](p:Parser[T], in:String):T = {
    val x = phrase(p)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
    }
  }

}



object ScamlParser {
  def main(args: Array[String]) = {
    val in = IOUtil.loadTextFile(new File(args(0)))
    val p = new ScamlParser
    println(p.phrase(p.parser)(new CharSequenceReader(in)))
  }

}                                 