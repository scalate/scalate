/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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
package org.fusesoruce.scalate.haml

import scala.util.parsing.combinator._
import util.parsing.input.{Positional, CharSequenceReader}
import scala.None
import org.fusesource.scalate.{TemplateException}

/**
 * Base class for parsers which use indentation to define
 * structure.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class IndentedParser extends RegexParsers() {

  var skipWhitespaceOn = false
  override def skipWhitespace = skipWhitespaceOn

  def skip_whitespace[T](p: => Parser[T]): Parser[T] = Parser[T] { in =>
    skipWhitespaceOn = true
    val result = p(in)
    skipWhitespaceOn = false
    result
  }

  var indent_unit:Parser[Any] = null
  var indent_level:Int = 1


  def current_indent(): Parser[Any] = {
    if( indent_level==0 ) {
      success()
    } else if( indent_unit!=null ) {
      repN(indent_level,indent_unit)
    } else {
      ( """ +""".r | """\t+""".r) ^^ ( s=>{
          indent_unit=s;
          s
        } | failure("indent not found") )
    }
  }

  def indent[U](p:Parser[U]) =
    ( current_indent ^^ { s=>{ indent_level+=1;s}  } ) ~> p ^^ { s=>{indent_level-=1;  s}}

}

object Trim extends Enumeration {
  val Outer, Inner, Both = Value
}

trait Statement extends Positional
trait TextExpression extends Statement

case class EvaluatedText(code:String, body:List[Statement], preserve:Boolean, sanitise:Option[Boolean]) extends TextExpression
case class LiteralText(text:List[String], sanitise:Option[Boolean]) extends TextExpression
case class Element(tag:Option[String], attributes:List[(Any,Any)], text:Option[TextExpression], body:List[Statement], trim:Option[Trim.Value], close:Boolean) extends Statement
case class ScamlComment(text:Option[String], body:List[String]) extends Statement
case class HtmlComment(conditional:Option[String], text:Option[String], body:List[Statement]) extends Statement
case class Executed(code:Option[String], body:List[Statement]) extends Statement
case class Filter(filter:String, body:List[String]) extends Statement
case class Attribute(kind:String, name: String, className: String, defaultValue: Option[String], autoImport:Boolean) extends Statement
case class Doctype(line:List[String]) extends Statement

/**
 * Parses a HAML/Scala based document.  Original inspired by the ruby version at http://haml-lang.com/
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ScamlParser extends IndentedParser() {

  /** once p1 is matched, disable backtracking.  Comsumes p1. Yeilds the result of p2 */
  def prefixed[T, U]( p1:Parser[T], p2:Parser[U] ) = p1.~!(p2) ^^ { case _~x => x }
  /** once p1 is matched, disable backtracking.  Does not comsume p1. Yeilds the result of p2 */
  def guarded[T, U]( p1:Parser[T], p2:Parser[U] ) = guard(p1)~!p2 ^^ { case _~x => x }

  def upto[T]( p1:Parser[T]):Parser[String] = {
    rep1( not( p1 ) ~> ".".r ) ^^ { _.mkString("") }
  }

  def wrapped[T,U](prefix:Parser[T], postfix:Parser[U]):Parser[String] = {
    prefixed( prefix, upto(postfix) <~ postfix )
  }

  val any                     = """.*""".r
  val nl                      = """\r?\n""".r
  val any_space_then_nl      ="""[ \t]*\r?\n""".r
  val tag_ident              = """[a-zA-Z_0-9][\:a-zA-Z_0-9]*""".r
  val word                    = """[a-zA-Z_0-9]+""".r
  val text                    = """.+""".r
  val space                   = """[ \t]+""".r
  val some_space              = """[ \t]*""".r

  val ident                   = """[a-zA-Z_]\w*""".r
  val qualified_type          = """[a-zA-Z0-9\$_\[\]\.]+""".r
  val scala_string_literal    = "\""~>"""([^"\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*""".r<~"\""
  val ruby_string_literal     = "'"~>"""([^'\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*""".r<~"'"
  val string_literal          = scala_string_literal | ruby_string_literal

  val whole_number            = """-?\d+""".r
  val decimal_number          = """(\d+(\.\d*)?|\d*\.\d+)""".r
  val floating_point_number   = """-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r

  // Haml hash style attributes are any valid ruby hash expression. The scala version should
  // either accept the same to allow easy migration of existing haml pages, or accept a
  // valid scala Map expression.
  //
  def hash_style_attributes = prefixed("{" ~ some_space, repsep(hash_attribute_entry,"""[ \t]*,\s*""".r)) <~ some_space ~ "}"
  def hash_attribute_entry: Parser[(Any, Any)] = (expression <~ some_space) ~ ("=>" ~ some_space ~> expression) ^^ { case key~value => (key, value) }
  def expression: Parser[Any] = string_literal | whole_number | decimal_number | floating_point_number | symbol | tag_ident | attributes
  def symbol = ":"~>ident 

  def html_attribute_entry: Parser[(Any, Any)] = (tag_ident <~ some_space) ~ ("=" ~some_space ~> string_literal) ^^ { case key~value => (key, value) }
  def html_style_attributes = prefixed("("~some_space, repsep(html_attribute_entry,"""\s+""".r)) <~ some_space~")"

  def class_entry:Parser[(Any, Any)] = "." ~> word ^^ { case x=> ("class", x) }
  def id_entry:Parser[(Any, Any)] = "#" ~> word ^^ { case x=> ("id", x) }

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
    prefixed("=", upto(nl) <~ nl) ^^ { x=> Some(EvaluatedText(x, List(), false, None)) } |
    space ~ nl ^^ { x=>None } |
    nl ^^ { x=>None } |
    space ~> literal_text(None) <~ any_space_then_nl ^^ { x=>Some(x) }

  def full_element_statement:Parser[Element] =
    opt("%"~>tag_ident) ~ attributes ~ opt(trim)  <~ ( "/" ~! some_space ~ nl ) ^^ {
      case (tag~attributes~wsc) => Element(tag, attributes, None, List(), wsc, true)
    } |
    opt("%"~>tag_ident) ~ attributes ~ opt(trim) ~ element_text ~ rep(indent(statement)) ^^ {
        case ((tag~attributes~wsc~text)~body) => Element(tag, attributes, text, body, wsc, false)
    }

  def element_statement:Parser[Element] = guarded("%"|"."|"#", full_element_statement)

  def haml_comment_statement = prefixed("-#", opt(some_space~>text)<~nl) ~ rep(indent(any<~nl)) ^^ { case text~body=> ScamlComment(text,body) }
  def html_comment_statement = prefixed("/", opt(prefixed("[", upto("]") <~"]")) ~ opt(some_space~>text)<~nl ) ~ rep(indent(statement)) ^^ { case conditional~text~body=> HtmlComment(conditional,text,body) }

  def evaluated_fragment:Parser[List[String]]  = wrapped("#{", "}") ~ opt(litteral_fragment) ^^ {
    case code~Some(text)=>{ code :: text }
    case code~None=>{ code :: Nil }
  }
  val litteral_fragment:Parser[List[String]] = opt(upto("#{"|any_space_then_nl)) ~ opt(evaluated_fragment) ^^ {
    case None~Some(code)=>{ "" :: code }
    case None~None=>{ "" :: Nil }
    case Some(text)~Some(code)=>{ text :: code }
    case Some(text)~None=>{ text :: Nil }
  }

  def literal_text(sanitize:Option[Boolean]) = litteral_fragment ^^ { LiteralText(_, sanitize) }

  def text_statement = (
          prefixed("""\""", literal_text(None))      |
          prefixed("&=="~some_space, literal_text(Some(true)) )  |
          prefixed("!=="~some_space, literal_text(Some(false)) ) |
          prefixed("&"~space, literal_text(Some(true)) )  |
          prefixed("!"~space, literal_text(Some(false)) ) |
          literal_text(None)
        ) <~ any_space_then_nl

  def evaluated_statement =
    prefixed("=",  upto(nl) <~ nl ) ~ rep(indent(statement)) ^^ { case code~body => EvaluatedText(code, body, false, None) }       |
    prefixed("~",  upto(nl) <~ nl ) ~ rep(indent(statement)) ^^ { case code~body => EvaluatedText(code, body, true,  None) }       |
    prefixed("&=", upto(nl) <~ nl ) ~ rep(indent(statement)) ^^ { case code~body => EvaluatedText(code, body, false, Some(true)) } |
    prefixed("!=", upto(nl) <~ nl ) ~ rep(indent(statement)) ^^ { case code~body => EvaluatedText(code, body, false, Some(false)) }


  val attribute = skip_whitespace( opt("import") ~ ("var"|"val") ~ ident ~ (":" ~> qualified_type) ~ (opt("=" ~> text ))) ^^ {
    case p_import~p_kind~p_name~p_type~p_default => Attribute(p_kind, p_name, p_type, p_default, p_import.isDefined)
  }

  def attribute_statement = prefixed("-@", attribute <~ nl) 

  def executed_statement = prefixed("-", opt(text) <~ nl) ~ rep(indent(statement)) ^^ {
    case code~body=> Executed(code,body)
  }

  def filter_statement = prefixed(":", text <~ nl) ~ rep(indent(any<~nl)) ^^ { case code~body=> Filter(code,body) }

  def doctype_statement = prefixed("!!!", rep(some_space ~> """[^ \t \r \n]+""".r) <~ some_space ~ nl) ^^ { Doctype(_) }

  def statement:Parser[Statement] =
      positioned(haml_comment_statement) |
      positioned(html_comment_statement) |
      positioned(element_statement) |
      positioned(evaluated_statement) |
      positioned(attribute_statement) |
      positioned(executed_statement) |
      positioned(doctype_statement) |
      positioned(text_statement)

  def parser = rep( statement )

  def parse(in:String) = {
    var content = in;
    if( !in.endsWith("\n") ) {
      content = in + "\n"
    }
    val x = phrase[List[Statement]](parser)(new CharSequenceReader(content))
    x match {
      case Success(result, _) => result
      case _ => throw new TemplateException(x.toString);
    }
  }

}

object ScamlParser {
  def main(args: Array[String]) = {
     val in = """!!! XML
!!! 
%div#things
"""
    val p = new ScamlParser
    println(p.phrase(p.parser)(new CharSequenceReader(in)))
  }

}