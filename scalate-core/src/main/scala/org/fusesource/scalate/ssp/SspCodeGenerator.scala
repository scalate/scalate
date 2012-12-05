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
package org.fusesource.scalate.ssp

import org.fusesource.scalate._
import collection.mutable.Stack
import support.{Text, Code, AbstractCodeGenerator}

class SspCodeGenerator extends AbstractCodeGenerator[PageFragment] {
  import scala.language.implicitConversions
  
  override val stratumName = "SSP"

  implicit def textToString(text: Text) = text.value

  implicit def textOptionToString(text: Option[Text]): Option[String] = text match {
    case None => None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[PageFragment] {

    def generate(fragments: List[PageFragment]): Unit = {
      var remaining = fragments
      while( remaining != Nil ) {
        val fragment = remaining.head
        remaining = remaining.drop(1)

        fragment match {

          case p: AttributeFragment =>
            this << p.pos;
            generateBindings(List(Binding(p.name, p.className, p.autoImport, p.defaultValue,
              classNamePositional = Some(p.className), defaultValuePositional = p.defaultValue))) {
              generate(remaining)
            }
            remaining = Nil

          case _ =>
            generate(fragment)
        }
      }
    }

    def generate(fragment: PageFragment): Unit = {
      fragment match {
        case CommentFragment(code) => {
        }
        case ScriptletFragment(code) => {
          //this << code.pos;
          this << code :: Nil
        }
        case TextFragment(text) => {
          this << fragment.pos;
          this << "$_scalate_$_context << ( " + asString(text) + " );"
        }
        case af: AttributeFragment => {
        }
        case DollarExpressionFragment(code) => {
          this << "$_scalate_$_context <<< " :: wrapInParens(code)
        }
        case ExpressionFragment(code) => {
          this << "$_scalate_$_context <<< " :: wrapInParens(code)
        }
        case IfFragment(code) => {
          this << code.pos;
          this << "if (" + code + ") {"
          indentLevel += 1
        }
        case DoFragment(code) => {
          if (code.length > 0) {
            this << "$_scalate_$_context << " :: code :: " {" :: Nil
          }
          else {
            this << code.pos;
            this << "{"
          }
          indentLevel += 1
        }
        case ElseIfFragment(code) => {
          indentLevel -= 1
          this << "} else if (" :: code :: ") {" :: Nil
          indentLevel += 1
        }
        case code: ElseFragment => {
          this << code.pos;
          indentLevel -= 1
          this << "} else {"
          indentLevel += 1
        }
        case MatchFragment(code) => {
          this << "(" :: code :: ") match {" :: Nil
          indentLevel += 1
        }
        case CaseFragment(code) => {
          indentLevel -= 1
          this << "case " :: code :: " =>" :: Nil
          indentLevel += 1
        }
        case code: OtherwiseFragment => {
          this << code.pos;
          indentLevel -= 1
          this << "case _ =>"
          indentLevel += 1
        }
        case ForFragment(code) => {
          this << "for (" :: code :: ") {" :: Nil
          indentLevel += 1
        }
        case SetFragment(code) => {
          this << "$_scalate_$_context.attributes(\"" + (code.trim) + "\") = capture {" :: Nil
          indentLevel += 1
        }
        case ImportFragment(code) => {
          this << "import " :: code :: Nil
        }
        case code: EndFragment => {
          this << code.pos;
          indentLevel -= 1
          this << "}"
        }
      }
    }

    protected def wrapInParens(code: Text): List[_] = if (canWrapInParens(code)) {List("( ", code, " );")} else {List(code)}

    /**
     * Returns true if the code expression can be safely wrapped in parens
     */
    protected def canWrapInParens(code: String) = {
      val lastChar = code.trim.takeRight(1)
      lastChar != "{" && lastChar != "("
    }
  }

  override def generate(engine: TemplateEngine, source: TemplateSource, bindings: Traversable[Binding]): Code = {

    // Load the translation unit
    val content = source.text
    val uri = source.uri

    // Parse the translation unit
    val rawFragments = (new SspParser).getPageFragments(content)

    checkSyntax(rawFragments)

    val fragments = transformSyntax(rawFragments)

    val sb = new SourceBuilder
    sb.generate(engine, source, bindings, fragments)

    Code(source.className, sb.code, Set(uri), sb.positions)
  }

  /**
   * lets check that the syntax is correct
   */
  protected def checkSyntax(fragments: List[PageFragment]): Unit = {
    val endStack = new Stack[PageFragment]
    var clauseOpen = true

    def open(f: PageFragment): Unit = {
      endStack.push(f)
      clauseOpen = true
    }
    def expect(f: PageFragment, expectedType: Class[_], name: String, closeName: String, closes: Boolean): Unit = if (endStack.isEmpty) {
      throw new InvalidSyntaxException("Missing " + name, f.pos)
    } else {
      if (closes) {
        // closing clause like #else
        if (!clauseOpen) {
          throw new InvalidSyntaxException("Cannot have more than one " + f.tokenName + " within a single #" + name, f.pos)
        }
        clauseOpen = false
      }
      else {
        // non close like #eliseif
        if (!clauseOpen) {
          throw new InvalidSyntaxException("The " + f.tokenName + " cannot come after the #" + closeName + " inside the #" + name, f.pos)
        }
      }
      val head = endStack.head
      if (!expectedType.isInstance(head)) {
        throw new InvalidSyntaxException("The " + f.tokenName + " should be nested inside #" + name + " but was inside " + head.tokenName, f.pos)
      }
    }

    for (f <- fragments) f match {
      case f: ForFragment => open(f)
      case f: SetFragment => open(f)
      case f: DoFragment => open(f)
      case f: IfFragment => open(f)
      case f: MatchFragment => open(f)
      case f: EndFragment => if (endStack.isEmpty) {
        throw new InvalidSyntaxException("Extra #end without matching #if, #for, #match", f.pos)
      } else {
        endStack.pop
      }
      case f: ElseIfFragment => expect(f, classOf[IfFragment], "if", "else", false)
      case f: ElseFragment => expect(f, classOf[IfFragment], "if", "else", true)
      case f: CaseFragment => expect(f, classOf[MatchFragment], "match", "otherwise", false)
      case f: OtherwiseFragment => expect(f, classOf[MatchFragment], "match", "otherwise", true)

      // TODO check that else within if and no else if after else
      case _ =>
    }
    if (!endStack.isEmpty) {
      val f = endStack.head
      throw new InvalidSyntaxException("Missing #end for " + f.tokenName, f.pos)
    }
  }

  protected def transformSyntax(fragments: List[PageFragment]) = {
    var last: PageFragment = null
    def isMatch = last != null && last.isInstanceOf[MatchFragment]

    fragments.filter {
      _ match {
        case t: TextFragment if (isMatch) =>
          val trim = t.text.trim
          if (trim.length == 0) {
            false
          }
          else {
            throw new InvalidSyntaxException("Only whitespace allowed between #match and #case but found '" + trim + "'", t.pos)
          }

        case p =>
          if (isMatch) {
            if (!p.isInstanceOf[CaseFragment] && !p.isInstanceOf[OtherwiseFragment]) {
              throw new InvalidSyntaxException("Only whitespace allowed between #match and #case but found " + p.tokenName, p.pos)
            }
          }
          last = p
          true
      }
    }
  }
}

