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
package org.fusesource.scalate.shaml

import org.fusesoruce.scalate.haml._
import java.util.regex.Pattern
import java.net.URI
import org.fusesource.scalate._

/**
 * Generates a scala class given a HAML document
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class HamlCodeGenerator extends AbstractCodeGenerator[Statement] {

  private class SourceBuilder extends AbstractSourceBuilder[Statement] {

    def generate(statements:List[Statement]):Unit = {
      statements.foreach(statement=>{
        generate(statement)
      })
    }

    def generate(statement:Statement):Unit = {
      statement match {
        case s:HamlComment=> {
          this << "// "+s.text.getOrElse("")
        }
        case s:HtmlComment=> {
          this << "$_scalate_$_out << ( "+asString("<!--"+s.text.getOrElse("")+"-->")+" );"
        }
        case s:LiteralText=> {
          this << "$_scalate_$_out << ( "+asString(s.text)+" );"
        }
        case s:EvaluatedText=> {
          this << "$_scalate_$_out << ( "+s.code+");"
        }
        case s:Element=> {
          generate(s)
        }
        case s:Executed=> {
          generate(s)
        }
        case s:Filter=> {
          throw new UnsupportedOperationException("filters not yet implemented.");
        }
      }
    }

    def generate(statement:Executed):Unit = {
      statement match {
        case Executed(Some(code), List()) => {
          this << code
        }
        case Executed(Some(code), list) => {
          this << code + " {"
          indent {
            generate(list)
          }
          this << "}"
        }
        case Executed(None,List())=> {}
      }
    }

    def generate(statement:Element):Unit = {
      val tag = statement.tag.getOrElse("div");
      this << "$_scalate_$_out << ( "+asString("<"+tag+attributes(statement.attributes)+">")+" );"
      statement match {
        case Element(_,_,None, List(), _, _) => {}
        case Element(_,_,Some(text), List(), _, _) => {
          generate(text)
        }
        case Element(_,_,None, list, _, _) => {
          generate(list)
        }
        case _ => throw new IllegalArgumentException("Syntax error on line "+statement.pos.line+": Illegal nesting: content can't be both given on the same line as html element and nested within it.");
      }
      this << "$_scalate_$_out << ( "+asString("</"+tag+">")+" );"
    }

    def attributes(entries: List[(Any,Any)]) = {
      val (entries_class, entries_rest) = entries.partition{x=>{ x._1 match { case "class" => true; case _=> false} } }
      var map = Map( entries_rest: _* )

      if( !entries_class.isEmpty ) {
        val value = entries_class.map(x=>x._2).mkString(" ")
        map += "class"->value
      }
      map.foldLeft(""){ case (r,e)=>r+" "+eval(e._1)+"=\""+eval(e._2)+"\""}
    }

    def eval(expression:Any) = {
      expression match {
        case s:String=>s
        case _=> throw new UnsupportedOperationException("don't know how to eval: "+expression);
      }
    }

  }


  override def generate(engine:TemplateEngine, uri:String, args:List[TemplateArg]): Code = {

    val hamlSource = engine.resourceLoader.load(uri)
    val (packageName, className) = extractPackageAndClassNames(uri)
    val statements = HamlParser.parse(hamlSource)
    val builder = new SourceBuilder()
    builder.generate(packageName, className, args, statements)
    Code(this.className(uri, args), builder.code, Set())

  }



}
