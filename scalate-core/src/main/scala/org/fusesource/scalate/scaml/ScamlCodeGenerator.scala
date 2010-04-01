/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate.scaml

import _root_.org.fusesource.scalate.scaml._
import java.util.regex.Pattern
import java.net.URI
import org.fusesource.scalate._
import collection.mutable.LinkedHashMap
import scala.util.parsing.input.CharSequenceReader
import support.{Code, AbstractCodeGenerator}
import util.RenderHelper
import util.RenderHelper
import collection.immutable.List

/**
 * Generates a scala class given a HAML document
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ScamlCodeGenerator extends AbstractCodeGenerator[Statement] {

  override val stratumName = "SCAML"
  
  implicit def textToString(text:Text) = text.value
  implicit def textOptionToString(text:Option[Text]):Option[String] = text match {
    case None=>None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[Statement] {

    val text_buffer = new StringBuffer
    var element_level = 0
    var pending_newline = false
    var suppress_indent = false
    var in_html_comment = false

    protected def isImportStatementOrCommentOrWhitespace(fragment: Statement) = fragment match {
      case s: Executed if (s.code.mkString(";").trim.startsWith("import ")) => true
      case s: LiteralText if (s.isStatic) => true
      case s: Doctype => true
      case s: ScamlComment => true
      case _ => false
    }

    override def current_position = {
      if( text_buffer.length==0 ) {
        super.current_position
      } else {
        super.current_position + ("$_scalate_$_context << ( "+asString(text_buffer.toString)).length
      }
    }


    def write_indent() = {
      if( pending_newline ) {
        text_buffer.append("\n")
        pending_newline=false;
      }
      if( suppress_indent ) {
        suppress_indent=false
      } else {
        text_buffer.append(indent_string)
      }
    }

    def indent_string() = {
      val rc = new StringBuilder
      for( i <- 0 until element_level ) {
        rc.append("  ")
      }
      rc.toString
    }

    def trim_whitespace() = {
      pending_newline=false
      suppress_indent=true
    }

    def write_text(value:String) = {
      text_buffer.append(value)
    }

    def write_nl() = {
      pending_newline=true
    }

    def flush_text() = {
      if( pending_newline ) {
        text_buffer.append("\n")
        pending_newline=false;
      }
      if( text_buffer.length > 0 ) {
        this << "$_scalate_$_context << ( "+asString(text_buffer.toString)+" );"
        text_buffer.setLength(0)
      }
    }

    override def generateInitialImports = {
      this << "import _root_.org.fusesource.scalate.util.RenderHelper.{sanitize=>$_scalate_$_sanitize, preserve=>$_scalate_$_preserve, indent=>$_scalate_$_indent, smart_sanitize=>$_scalate_$_smart_sanitize, attributes=>$_scalate_$_attributes}"
    }

    def generate(statements:List[Statement]):Unit = {
      generate_with_flush(statements)
    }

    def generate_with_flush(statements:List[Statement]):Unit = {
      generate_no_flush(statements)
      flush_text
    }

    def generate_no_flush(statements:List[Statement]):Unit = {

      val bindings = statements.flatMap {
        case attribute: Attribute => List(Binding(attribute.name.value, attribute.className.value, attribute.autoImport, attribute.defaultValue))
        case _ => Nil
      }

      generateBindings(bindings) {
        statements.foreach(statement=>{
          generate(statement)
        })
      }
    }

    def generate(statement:Statement):Unit = {
      statement match {
        case s:Newline=> {
        }
        case s:Attribute=> {
        }
        case s:ScamlComment=> {
          generate(s)
        }
        case s:TextExpression=> {
          generateTextExpression(s,true)
        }
        case s:HtmlComment=> {
          generate(s)
        }
        case s:Element=> {
          generate(s)
        }
        case s:Executed=> {
          generate(s)
        }
        case s:FilterStatement=> {
          generate(s)
        }
        case s:Doctype=>{
          generate(s)
        }
      }
    }

    def generate(statement:Doctype):Unit = {
      this << statement.pos;
      write_indent
      statement.line.map{_.value} match {
        case List("XML")=>
          write_text("<?xml version=\"1.0\" encoding=\"utf-8\" ?>")
        case List("XML", encoding)=>
          write_text("<?xml version=\"1.0\" encoding=\""+encoding+"\" ?>")
        case _=>
          ScamlOptions.format match {
            case ScamlOptions.Format.xhtml=>
              statement.line.map{_.value} match {
                case List("Strict")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">""")
                case List("Frameset")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">""")
                case List("5")=>
                  write_text("""<!DOCTYPE html>""")
                case List("1.1")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">""")
                case List("Basic")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd"> """)
                case List("Mobile")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.2//EN" "http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd">""")
                case _=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">""")
              }
            case ScamlOptions.Format.html4=>
              statement.line.map{_.value} match {
                case List("Strict")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">""")
                case List("Frameset")=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">""")
                case _=>
                  write_text("""<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">""")
              }
            case ScamlOptions.Format.html5=>
              write_text("""<!DOCTYPE html>""")
            
          }
      }
      write_nl
    }

    def generate(statement:FilterStatement):Unit = {

      def isEnabled(flag:String) = {
        statement.flags.contains(Text(flag))
      }
      if( isEnabled("&") && isEnabled("!") ) {
        throw new InvalidSyntaxException("Cannot use both the '&' and '!' filter flags together.", statement.pos);
      }

      val preserve = isEnabled("~")
      val interpolate = isEnabled("&") || isEnabled("!")
      val sanitize = interpolate && isEnabled("&")

      var content = statement.body.map{_.value}.mkString("\n")

      var text:TextExpression = if( interpolate ) {
        val p = new ScamlParser()
        p.parse(p.literal_text(Some(sanitize)), content)
      } else {
        LiteralText(List(Text(content)), Some(false))
      }

      write_indent
      flush_text

      var prefix = "$_scalate_$_context << ( "
      var suffix = ");"

      if( preserve ) {
        prefix += "$_scalate_$_preserve (  "
        suffix = ") " + suffix;
      } else {
        prefix += "$_scalate_$_indent ( "+asString(indent_string())+", "
        suffix = ") " + suffix;
      }

      for ( f <- statement.filters ) {
        prefix += "$_scalate_$_context.value ( _root_.org.fusesource.scalate.filter.FilterRequest("+asString(f)+", "
        suffix = ") ) " + suffix;
      }

      this << prefix + "$_scalate_$_context.capture { "
      indent {
        this << statement.pos;
        generateTextExpression(text, false)
        flush_text
      }
      this << "} "+suffix
      write_nl
    }


    def generateTextExpression(statement:TextExpression, is_line:Boolean):Unit = {
      statement match {
        case s:LiteralText=> {
          if( is_line ) {
            write_indent
          }
          var literal=true;
          for( part <- s.text ) {
            // alternate between rendering literal and interpolated text
            if( literal ) {
              write_text(part)
              literal=false
            } else {
              flush_text
              s.sanitize match {
                case None=>
                  if( ScamlOptions.escape_html ) {
                    this << "$_scalate_$_context << ( $_scalate_$_smart_sanitize ($_scalate_$_context, "+part+" ));"
                  } else {
                    this << "$_scalate_$_context << ( "+part+" );"
                  }
                case Some(true)=>
                  this << "$_scalate_$_context <<< ( "+part+" );"
                case Some(false)=>
                  this << "$_scalate_$_context << ( "+part+" );"
              }
              literal=true
            }
          }
          if( is_line ) {
            write_nl
          }
        }
        case s:EvaluatedText=> {

          var prefix = "$_scalate_$_context << ("
          var suffix = ");"

          if( s.preserve ) {
            if( s.ugly ) {
              suppress_indent=true
            } else {
              prefix += " $_scalate_$_preserve ("
              suffix = ") " + suffix;
            }
          } else {
            prefix += " $_scalate_$_indent ( "+asString(indent_string())+","
            suffix = ") " + suffix;
          }

          val takeValue = s.sanitize match {
            case None=>
              if( ScamlOptions.escape_html ) {
                prefix += " $_scalate_$_smart_sanitize ($_scalate_$_context, "
                suffix = ") " + suffix;
                false
              } else {
                true
              }
            case Some(true)=>
              prefix += " $_scalate_$_sanitize ( "
              suffix = ") " + suffix;
              true
            case Some(false)=>
              true
          }

          if( takeValue ) {
            prefix += " $_scalate_$_context.value ("
            suffix = ") " + suffix;
          }

          if( is_line ) {
            write_indent
          }
          flush_text
          if( s.body.isEmpty ) {
            this << prefix
            indent {
              this << s.code.pos
              this << s.code
            }
            this << suffix
          } else {
            this << prefix
            indent {
              this << s.code.pos
              this << s.code+" {"
              indent {
                generate_with_flush(s.body)
              }
              this << "}"
            }
            this << suffix
          }
          if( is_line ) {
            write_nl
          }
        }
      }
    }

    def generate(statement:Executed):Unit = {
      flush_text
      if( statement.body.isEmpty ) {
        statement.code.foreach {
          (line) =>
          this << line.pos
          this << line.value
        }
      } else {
        statement.code.foreach {
          (line) =>
          this << line.pos
          if( line ne statement.code.last ) {
            this << line.value
          } else {
            this << line.value + "{"
          }
        }
        indent {
          generate_no_flush(statement.body)
          flush_text
        }
        this << "}"
      }
    }

    def generate(statement:HtmlComment):Unit = {
      //  case class HtmlComment(conditional:Option[String], text:Option[String], body:List[Statement]) extends Statement
      var prefix = "<!--"
      var suffix = "-->"
      if( statement.conditional.isDefined ) {
        prefix = "<!--["+statement.conditional.get+"]>"
        suffix = "<![endif]-->"
      }

      // To support comment within comment blocks.
      if( in_html_comment ) {
        prefix = "" 
        suffix = ""
      } else {
        in_html_comment = true
      }


      statement match {
        case HtmlComment(_, text, List()) => {
          write_indent
          this << statement.pos;
          write_text(prefix+" ")
          if( text.isDefined ) {
            write_text(text.get)
          }
          write_text(" "+suffix)
          write_nl
        }
        case HtmlComment(_, None, list) => {
          write_indent
          this << statement.pos;
          write_text(prefix)
          write_nl

          element_level += 1
          generate_no_flush(list)
          element_level -= 1

          write_indent
          write_text(suffix)
          write_nl
        }
        case _ => throw new InvalidSyntaxException("Illegal nesting: content can't be both given on the same line as html comment and nested within it", statement.pos);
      }

      if( prefix.length!= 0 ) {
        in_html_comment = false
      }
    }


    def generate(statement:ScamlComment):Unit = {
      this << statement.pos;
      statement match {
        case ScamlComment(text, List()) => {
          this << "//" + text.getOrElse("")
        }
        case ScamlComment(text, list) => {
          this << "/*" + text.getOrElse("")
          list.foreach(x=>{
            this << " * " + x
          })
          this << " */"
        }
      }
    }
    
    def isAutoClosed(statement:Element) = {
      statement.text == None && statement.body.isEmpty &&
      statement.tag.isDefined && ScamlOptions.autoclose.contains(statement.tag.get.value)
    }

    def generate(statement:Element):Unit = {

      var tag = statement.tag.getOrElse("div");
      if( statement.text.isDefined && !statement.body.isEmpty ) {
        throw new InvalidSyntaxException("Illegal nesting: content can't be given on the same line as html element or nested within it if the tag is closed", statement.pos)
      }
      
      def write_start_tag = {
        write_text("<"+tag)
        write_attributes(statement.attributes)
        if( statement.close || isAutoClosed(statement) ) {
          write_text("/>")
        } else {
          write_text(">")
        }
      }

      def write_end_tag = {
        if( statement.close || isAutoClosed(statement) ) {
          write_text("")
        } else {
          write_text("</"+tag+">")
        }
      }

      statement.trim match {
        case Some(Trim.Outer)=>{
        }
        case Some(Trim.Inner)=>{}
        case Some(Trim.Both)=>{}
        case _ => {}
      }

      def outer_trim = statement.trim match {
        case Some(Trim.Outer)=>{ trim_whitespace; true}
        case Some(Trim.Both)=>{ trim_whitespace; true}
        case _ => { false}
      }

      def inner_trim = statement.trim match {
        case Some(Trim.Inner)=>{ trim_whitespace; true}
        case Some(Trim.Both)=>{ trim_whitespace; true}
        case _ => { false }
      }
      
      outer_trim
      this << statement.pos;
      write_indent
      write_start_tag

      statement match {
        case Element(_,_,text,List(),_,_) => {
          generateTextExpression(text.getOrElse(LiteralText(List(Text("")), Some(false))), false)
          write_end_tag
          write_nl
          outer_trim
        }
        case Element(_,_,None,list,_,_) => {
          write_nl

          if( !inner_trim ) {
            element_level += 1
          }
          generate_no_flush(list)
          if( !inner_trim ) {
            element_level -= 1
          }

          write_indent
          write_end_tag
          write_nl
          outer_trim
        }
        case _ => throw new InvalidSyntaxException("Illegal nesting: content can't be both given on the same line as html element and nested within it", statement.pos);
      }
    }

    def write_attributes(entries: List[(Any,Any)]) = {

      // Check to see if it's a dynamic attribute list
      var dynamic=false
      entries.foreach {
        (entry)=> entry._2 match {
          case x:EvaluatedText=>
            dynamic=true
          case x:LiteralText=>
            if( x.text.length > 1 ) {
              dynamic=true
            }
          case _=>
        }

      }
      if( dynamic ) {

        def write_expression(expression:Any) = {
          expression match {
            case s:Text=>s
              this << s.pos
              this << asString(s)
            case s:LiteralText=>
              this << "$_scalate_$_context.capture {"
              indent {
                generateTextExpression(s, false)
                flush_text
              }
              this << "}"
            case s:EvaluatedText=>
              this << s.code.pos
              if( s.body.isEmpty ) {
                this << s.code
              } else {
                this << s.code+" {"
                indent {
                  generate_with_flush(s.body)
                }
                this << "} "
              }
            case _=> throw new UnsupportedOperationException("don't know how to eval: "+expression);
          }
        }

        flush_text
        this << "$_scalate_$_context << $_scalate_$_attributes( $_scalate_$_context, List( ("
        indent {
          var first=true
          entries.foreach {
            (entry) =>
            if( !first ) {
              this << "), ("
            }
            first = false
            indent {
              write_expression(entry._1)
            }
            this << ","
            indent {
              write_expression(entry._2)
            }
          }
        }
        this << ") ) )"

      } else {

        def value_of(value:Any):Text = {
          value match {
            case LiteralText(text, _) => text.head
            case s:Text => s
            case _=> throw new UnsupportedOperationException("don't know how to deal with: "+value);
          }
        }

        val (entries_class, tmp) = entries.partition{x=>{ x._1 match { case "class" => true; case _=> false} } }
        val (entries_id, entries_rest) = tmp.partition{x=>{ x._1 match { case "id" => true; case _=> false} } }
        var map = LinkedHashMap[Text,Text]( )

        if( !entries_id.isEmpty ) {
          map += Text("id") -> value_of(entries_id.last._2)
        }

        if( !entries_class.isEmpty ) {
          var value:Option[Text] = None
          value = entries_class.foldLeft(value) {
            (rc, x)=> rc match {
              case None=>Some(value_of(x._2))
              case Some(y)=>Some(y+" "+value_of(x._2))
            }
          }
          map += Text("class")->value.get
        }

        entries_rest.foreach{ me => map += value_of(me._1) -> value_of(me._2) }


        if( !map.isEmpty ) {
          map.foreach {
            case (name,value) =>
            write_text(" ")
            this << name.pos
            write_text(name)
            write_text("=\"")
            this << value.pos
            write_text(RenderHelper.sanitize(value))
            write_text("\"")
          }
        }

      }
    }


  }


  override def generate(engine:TemplateEngine, uri:String, bindings:List[Binding]): Code = {

    val hamlSource = engine.resourceLoader.load(uri)
    val (packageName, className) = extractPackageAndClassNames(uri)
    val statements = (new ScamlParser).parse(hamlSource)

    val builder = new SourceBuilder()
    builder.generate(engine, packageName, className, bindings, statements)
    Code(this.className(uri), builder.code, Set(uri), builder.positions)
  }



}
