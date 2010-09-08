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
package org.fusesource.scalate.tool.commands

import org.osgi.service.command.CommandSession
import org.apache.felix.gogo.commands.{Action, Option => option, Argument => argument, Command => command}
import scala.xml._
import java.io.{OutputStream, FileOutputStream, PrintStream, File}
import collection.mutable.ArrayBuffer

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@command(scope = "scalate", name = "toscaml", description = "Converts an XML or HTML file to Scaml")
class ToScaml extends Action {

  @argument(index = 0, name = "from", description = "The input file. If ommited, input is read from the console")
  var from: File = _

  @argument(index = 1, name = "to", description = "The output file. If ommited, output is written to the console")
  var to: File = _

  var out:IndentPrintStream = _

  def execute(session: CommandSession): AnyRef = {

    def doit = {
      if( from!=null ) {
        process(XML.loadFile(from))
      } else {
        process(XML.load(session.getKeyboard))
      }
    }

    if( to!=null ) {
      out = new IndentPrintStream(new FileOutputStream(to));
      doit
      out.close()
    } else {
      out = new IndentPrintStream(session.getConsole);
      doit
      out.flush()
    }
    null
  }


  def to_text(line: String): String = {
    line
  }

  def to_element(tag: String): String = {
    "%" + tag
  }

  def process(value:Any):Unit = {

    val t = out
    import t._

    def tag(name:String) = {
      if( name.matches("""^[\w:_\-]+$""") ) {
        name
      } else {
        "'"+name+"'"
      }
    }

    value match {
      case x:Elem =>

        var id=""
        var clazz=""
        var atts=""

        def add(key:String, value:String) = {
          if( atts!="" ) {
            atts += " "
          }
          atts += key+"=\""+value+"\""
        }

        x.attributes.foreach{ a=>
          val key = a.key
          val value = a.value.toString
          if( key=="id" ) {
            if( value.matches("""^[\w_\-]+$""") )
              id = "#"+value
            else
              add(key,value)
          } else if( key=="class" ) {
            if( value.matches("""^[\w\s_\-]+$""") ) {
              value.split("""\s""").foreach{ c=>
                clazz += "."+c
              }
            } else {
              add(key,value)
            }
          } else {
            add(key,value)
          }
        }

        pi.p(to_element(tag(x.label))+id+clazz)
        if( atts!="" ) {
          p("("+atts+")")
        }

        x.child match {
          case Seq(x:Text) =>
            val value = x.text.trim
            if (value.contains("\r?\n")) {
              pl()
              indent {
                process(x)
              }
            } else {
              pl(" "+value)
            }
          case x =>
            pl()
            indent {
              x.foreach{ process _ }
            }
        }

      case x:Text =>
        val value = x.text.trim
        value.split("\r?\n").map(_.trim).foreach{ line =>
          if(line != "" ) {
            pi.pl(to_text(line))
          }
        }

      case x:AnyRef =>
        throw new Exception("Unhandled type: "+x.getClass);
    }
  }

  class IndentPrintStream(out:OutputStream) extends PrintStream(out) {
    var level=0
    def indent[T](op: => T): T = {level += 1; val rc = op; level -= 1; rc}

    def pi = { for (i <- 0 until level) { print("  ") }; this }
    def p(line: String) = { print(line); this }
    def pl(line: String="") = { println(line); this }
  }

}