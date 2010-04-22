/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 * Copyright (C) 2010, Progress Software Corporation and/or its
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

package org.fusesource.scalate.ssp

import org.fusesource.scalate._
import support.{Code, AbstractCodeGenerator}
import collection.mutable.Stack

class SspCodeGenerator extends AbstractCodeGenerator[PageFragment] {
  override val stratumName = "SSP"

  implicit def textToString(text: Text) = text.value

  implicit def textOptionToString(text: Option[Text]): Option[String] = text match {
    case None => None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[PageFragment] {
    protected def isImportStatementOrCommentOrWhitespace(fragment: PageFragment) = fragment match {
      case s: ScriptletFragment if (s.code.trim.startsWith("import ")) => true
      case s: TextFragment if (s.text.trim.length == 0) => true
      case s: CommentFragment => true
      case _ => false
    }

    def generate(fragments: List[PageFragment]): Unit = {
      fragments.foreach(generate)
    }

    def generate(fragment: PageFragment): Unit = {
      fragment match {
        case CommentFragment(code) => {
        }
        case ScriptletFragment(code) => {
          this << code.pos;
          this << code
        }
        case TextFragment(text) => {
          this << fragment.pos;
          this << "$_scalate_$_context << ( " + asString(text) + " );"
        }
        case af: AttributeFragment => {
        }
        case DollarExpressionFragment(code) => {
          this << code.pos;
          this << "$_scalate_$_context <<< " + wrapInParens(code)
        }
        case ExpressionFragment(code) => {
          this << code.pos;
          this << "$_scalate_$_context << " + wrapInParens(code)
        }
        case IfFragment(code) => {
          this << code.pos;
          this << "if (" + code + ") {"
          indentLevel += 1
        }
        case DoFragment(code) => {
          this << code.pos;
          if (code.length > 0) {
            this << "$_scalate_$_context << " + code + " {"
          }
          else {
            this << "{"
          }
          indentLevel += 1
        }
        case ElseIfFragment(code) => {
          this << code.pos;
          indentLevel -= 1
          this << "} else if (" + code + ") {"
          indentLevel += 1
        }
        case code: ElseFragment => {
          this << code.pos;
          indentLevel -= 1
          this << "} else {"
          indentLevel += 1
        }
        case MatchFragment(code) => {
          this << code.pos;
          this << "(" + code + ") match {"
          indentLevel += 1
        }
        case CaseFragment(code) => {
          this << code.pos;
          indentLevel -= 1
          this << "case " + code + " =>"
          indentLevel += 1
        }
        case code: OtherwiseFragment => {
          this << code.pos;
          indentLevel -= 1
          this << "case _ =>"
          indentLevel += 1
        }
        case ForFragment(code) => {
          this << code.pos;
          this << "for (" + code + ") {"
          indentLevel += 1
        }
        case ImportFragment(code) => {
          this << code.pos;
          this << "import " + code
        }
        case code: EndFragment => {
          this << code.pos;
          indentLevel -= 1
          this << "}"
        }
      }
    }

    protected def wrapInParens(code: String) = if (canWrapInParens(code)) {"( " + code + " )"} else {"" + code + ""}

    /**
     * Returns true if the code expression can be safely wrapped in parens
     */
    protected def canWrapInParens(code: String) = {
      val lastChar = code.trim.takeRight(1)
      lastChar != "{" && lastChar != "("
    }
  }

  override def generate(engine: TemplateEngine, source: TemplateSource, bindings: List[Binding]): Code = {

    // Load the translation unit
    val content = source.text
    val uri = source.uri

    // Determine the package and class name to use for the generated class
    val (packageName, className) = extractPackageAndClassNames(uri)

    // Parse the translation unit
    val rawFragments = (new SspParser).getPageFragments(content)

    checkSyntax(rawFragments)

    val fragments = transformSyntax(rawFragments)

    // Convert the parsed AttributeFragments into Binding objects
    val templateBindings = fragments.flatMap {
      case p: AttributeFragment => List(Binding(p.name, p.className, p.autoImport, p.defaultValue))
      case _ => Nil
    }

    val sb = new SourceBuilder
    sb.generate(engine, packageName, className, bindings ::: templateBindings, fragments)

    Code(this.className(uri), sb.code, Set(uri), sb.positions)
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

