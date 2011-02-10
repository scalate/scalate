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
package org.fusesource.scalate.converter

import org.fusesource.scalate.support.Text
import util.matching.Regex.Match
import org.fusesource.scalate.util.Log


object ExpressionLanguage {
  protected val operators = Map("eq" -> "==", "ne" -> "!=",
    "gt" -> ">", "ge" -> ">=",
    "lt" -> "<", "le" -> "<=",
    "not" -> "!")

  protected val notEmptyRegex = """(\s|^)(not\s+empty)\s(.+)""".r
  protected val emptyRegex = """(\s|^)(empty)\s(.+)""".r
  protected val lengthRegex = """fn:length\((.+)\)""".r

  def asScala(el: String): String = {
    // lets switch the EL style indexing to Scala parens and switch single quotes to doubles
    var text = el.replace('[', '(').
            replace(']', ')').
            replace('\'', '\"')


    def space(m: Match): String = if (m.start == 0) "" else " "

    // "not empty xxx" => "!(xxx isEmpty)"
    text = notEmptyRegex.replaceAllIn(text, {m => space(m) + "!(" + m.subgroups.last + " isEmpty)"})

    // "empty whatever" => "whatever isEmpty"
    text = emptyRegex.replaceAllIn(text, {m => space(m) + m.subgroups.last + " isEmpty"})

    // replace EL operators
    for ((a, b) <- operators) {
      text = text.replaceAll("(\\s|^)" + a + "\\s", " " + b + " ")
    }

    // foo.bar => foo.getBar
    var first = true
    text = text.split('.').map(s =>
      if (!first && s.length > 0 && s(0).isUnicodeIdentifierStart) {
        "get" + s.capitalize
      } else {
        first = false
        s
      }).mkString(".")

    // fn:length(foo) => foo.size
    text = lengthRegex.replaceAllIn(text, {m => m.subgroups.last + ".size"})

    text
  }
}

trait IndentWriter {
  var out = new StringBuilder
  var indentLevel: Int = 0
  var indentText: String = "  "

  def reset: Unit = {
    out = new StringBuilder
  }

  def indent[T](op: => T): T = {indentLevel += 1; val rc = op; indentLevel -= 1; rc}

  def println(line: String): this.type = {
    for (i <- 0 until indentLevel) {
      print(indentText)
    }
    print(line)
    println
    this
  }

  def print(value: AnyRef): Unit = {
    out.append(value)
  }

  def println = print("\n")


  def text = out.toString
}
object JspConverter extends Log
class JspConverter extends IndentWriter  {
  import JspConverter._

  var coreLibraryPrefix: String = "c"
  var whenCount = 0
      
  def convert(jsp: String): String = {
    reset

    val parser = new JspParser
    val result = parser.parsePage(jsp)
    convert(result)
    text
  }

  def convert(list: List[PageFragment]): Unit = {
    for (r <- list) {
      convert(r)
    }
  }

  def convert(fragment: PageFragment): Unit = fragment match {
    case e: Element => transform(e)
    case _ => print(fragment.toString)
  }

  def transform(e: Element): Unit = {
    e match {

    // core JSTL library
      case Element(QualifiedName(coreLibraryPrefix, name), attributes, body) =>
        name match {
          case "choose" =>
            whenCount = 0
            convert(body)
            print("#end")

          case "forEach" =>
            val varExp = e.attributeMap.getOrElse("var", textExpression("i"))
            print("#for(" + asUnquotedParam(varExp) + " <- ")

            e.attributeMap.get("items") match {
              case Some(exp) =>
                print(asParam(exp) + ")")

              case _ =>
                val begin = e.attribute("begin")
                val end = e.attribute("end")
                print(asUnquotedParam(begin) + ".to(" + asUnquotedParam(end))

                e.attributeMap.get("step") match {
                  case Some(step) => print(", " + asUnquotedParam(step))
                  case _ =>
                }
                print("))")
            }
            convert(body)
            print("#end")

          case "if" =>
            val exp = e.attribute("test")
            print("#if(" + asParam(exp) + ")")
            convert(body)
            print("#end")

          case "otherwise" =>
            print("#else")
            convert(body)

          case "out" =>
            val exp = e.attribute("value")
            print("${")
            e.attributeMap.get("escapeXml") match {
              case Some(TextExpression(Text("true"))) => print("escape(" + asParam(exp) + ")")
              case Some(TextExpression(Text("false"))) => print("unescape(" + asParam(exp) + ")")
              case Some(e) => print("value(" + asParam(exp) + ", " + asUnquotedParam(e) + ")")
              case _ => print(asParam(exp))
            }
            print("}")


          case "set" =>
            val exp = e.attribute("value")
            val name = e.attribute("var")
            print("#{ var " + asUnquotedParam(name) + " = " + asParam(exp) + " }#")

          case "when" =>
            val exp = e.attribute("test")
            print("#" + (if (whenCount == 0) "if" else "elseif") + "(" + asParam(exp) + ")")
            whenCount +=1
            convert(body)

          case "url" =>
            val exp = e.attribute("value")
            print("${uri(" + asParam(exp) + ")}")

          case _ =>
            warn("No converter available for tag <" + coreLibraryPrefix + ":" + name + ">: " + e)
            print(e)
        }
      case _ => print(e)
    }
  }

  def print(e: Element): Unit = {
    print("<" + e.qualifiedName)
    for (a <- e.attributes) {
      print(" " + a.name + "=\"" + asParam(a.value) + "\"")
    }
    print("/>")
  }

  protected def textExpression(s: String) = TextExpression(Text(s))

  /**
   * Returns the text of an expression as a numeric method parameter
   */
  protected def asUnquotedParam(exp: Expression): String = exp.asUnquotedParam

  /**
   * Returns the text of an expression as a method parameter
   */
  protected def asParam(exp: Expression): String = exp.asParam
  /**
   * Returns the text of an expression as a method parameter
   */
  protected def asJsp(exp: Expression): String = exp.asJsp

}