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
package org.fusesoruce.ssp.haml

import scala.util.parsing.combinator._
import util.parsing.input.{Positional, CharSequenceReader}

/**
 * Base class for parsers which use indentation to define
 * structure.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class IndentedParser extends RegexParsers() {

  override def skipWhitespace = false


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

case class EvaluatedText(code:String, preserve:Boolean, sanitise:Option[Boolean]) extends TextExpression
case class LiteralText(text:String, sanitise:Option[Boolean]) extends TextExpression
case class Element(tag:Option[String], attributes:List[(Any,Any)], text:Option[TextExpression], body:List[Statement], trim:Option[Trim.Value], close:Boolean) extends Statement
case class HamlComment(text:Option[String], body:List[String]) extends Statement
case class HtmlComment(conditional:Option[String], text:Option[String], body:List[Statement]) extends Statement
case class Executed(code:Option[String], body:List[Statement]) extends Statement
case class Filter(filter:String, body:List[String]) extends Statement


/**
 * Parses a HAML/Scala based document.  Original inspired by the ruby version at http://haml-lang.com/
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object HamlParser extends IndentedParser() {

  /** once p1 is matched, disable backtracking.  Comsumes p1. Yeilds the result of p2 */
  def prefixed[T, U]( p1:Parser[T], p2:Parser[U] ) = p1.~!(p2) ^^ { case _~x => x }
  /** once p1 is matched, disable backtracking.  Does not comsume p1. Yeilds the result of p2 */
  def guarded[T, U]( p1:Parser[T], p2:Parser[U] ) = guard(p1)~!p2 ^^ { case _~x => x }

  val any                     = """.*""".r
  val nl                      = """\r?\n""".r
  val word                    = """[a-zA-Z_0-9]+""".r
  val text                    = """.+""".r
  val space                   = """[ \t]+""".r
  val some_space                   = """[ \t]*""".r

  val ident                   = """[a-zA-Z_]\w*""".r
  val string_literal          = "\""~>"""([^"\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*""".r<~"\""
  val whole_number            = """-?\d+""".r
  val decimal_number          = """(\d+(\.\d*)?|\d*\.\d+)""".r
  val floating_point_number   = """-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r

  // Haml hash style attributes are any valid ruby hash expression. The scala version should
  // either accept the same to allow easy migration of existing haml pages, or accept a
  // valid scala Map expression.
  //
  def hash_style_attributes = prefixed("{" ~ some_space, repsep(hash_attribute_entry,"""[ \t]*,\s*""".r)) <~ some_space ~ "}"
  def hash_attribute_entry: Parser[(Any, Any)] = (expression <~ some_space) ~ ("=>" ~ some_space ~> expression) ^^ { case key~value => (key, value) }
  def expression: Parser[Any] = string_literal | symbol | whole_number | decimal_number | floating_point_number | ident | attributes
  def symbol = ":"~>ident 

  def html_attribute_entry: Parser[(Any, Any)] = (ident <~ some_space) ~ ("=" ~some_space ~> string_literal) ^^ { case key~value => (key, value) }
  def html_style_attributes = prefixed("("~some_space, repsep(html_attribute_entry,"""\s+""".r)) <~ some_space~")"

  def class_entry:Parser[(Any, Any)] = "." ~> word ^^ { case x=> ("class", x) }
  def id_entry:Parser[(Any, Any)] = "#" ~> word ^^ { case x=> ("id", x) }

  def attributes =
          (rep(class_entry|id_entry)) ~
          (rep(hash_style_attributes|html_style_attributes)^^ {x=>x.flatMap{y=>y}}) ^^
          { case l1~l2 => l1:::l2 }

  def trim: Parser[Trim.Value] =
      ">" ^^{ s=> Trim.Outer } |
      "<" ^^{ s=> Trim.Inner } |
      ( ( "<>" | "><" ) ^^ { s=> Trim.Both } )

  def element_text = prefixed("=", text)  ^^ { EvaluatedText(_, false, None) } |
                     some_space ~> text ^^  { LiteralText(_,None) }

  def full_element_statement:Parser[Element] =
    opt("%"~>word) ~ attributes ~ opt(trim)  <~ ( "/" ~! some_space ~ nl ) ^^ {
      case (tag~attributes~wsc) => Element(tag, attributes, None, List(), wsc, true)
    } |
    opt("%"~>word) ~ attributes ~ opt(trim) ~ ( opt(element_text) <~ some_space ~ nl ) ~ rep(indent(statement)) ^^ {
        case ((tag~attributes~wsc~text)~body) => Element(tag, attributes, text, body, wsc, false)
    }

  def element_statement:Parser[Element] = guarded("%"|"."|"#", full_element_statement)

  def haml_comment_statement = prefixed("-#", opt(any<~nl)) ~ rep(indent(any<~nl)) ^^ { case text~body=> HamlComment(text,body) }
  def html_comment_statement = prefixed("/", opt("["~> any <~"]")) ~ opt(any<~nl) ~ rep(indent(statement)) ^^ { case conditional~text~body=> HtmlComment(conditional,text,body) }

  def text_statement = (
          prefixed("""\""", any) ^^ { LiteralText(_, None) } |
          prefixed("&==", any )  ^^ { LiteralText(_, Some(true)) } |
          prefixed("&==", any )  ^^ { LiteralText(_, Some(false)) } |
          any                    ^^ { LiteralText(_, None) }
        ) <~ nl

  def evaluated_statement = (
          prefixed("=", any )   ^^ { EvaluatedText(_, false, None) } |
          prefixed("~", any )   ^^ { EvaluatedText(_, true, None) } |
          prefixed("&=", any )  ^^ { EvaluatedText(_, false, Some(true)) } |
          prefixed("&=", any )  ^^ { EvaluatedText(_, false, Some(false)) } 
        ) <~ nl

  def executed_statement = prefixed("-", opt(text) <~ nl) ~ rep(indent(statement)) ^^ { case code~body=> Executed(code,body) }
  def filter_statement = prefixed(":", text <~ nl) ~ rep(indent(any<~nl)) ^^ { case code~body=> Filter(code,body) }

  def statement:Parser[Statement] =
      positioned(haml_comment_statement) |
      positioned(html_comment_statement) |
      positioned(element_statement) |
      positioned(evaluated_statement) |
      positioned(executed_statement) |
      positioned(text_statement)

  def parser = rep( statement )

  def parse(in:String) = {
    val x = phrase[List[Statement]](parser)(new CharSequenceReader(in))
    x match {
      case Success(result, _) => result
      case _ => throw new IllegalArgumentException(x.toString);
    }
  }

  def main(args: Array[String]) = {
     val in = """%a{:href => "/scala/standalone.ssp"}
"""
     println(parse(in))
   }
}