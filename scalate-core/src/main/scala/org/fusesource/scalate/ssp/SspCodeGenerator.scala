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
          // TODO indent
          this << "if (" + code + ") {"
        }
        case ElseIfFragment(code) => {
          this << code.pos;
          // TODO indent
          this << "} else if (" + code + ") {"
        }
        case code: ElseFragment => {
          this << code.pos;
          // TODO indent
          this << "} else {"
        }
        case MatchFragment(code) => {
          this << code.pos;
          // TODO indent
          this << "(" + code + ") match {"
        }
        case CaseFragment(code) => {
          this << code.pos;
          this << "case " + code + " =>"
        }
        case code: OtherwiseFragment => {
          this << code.pos;
          this << "case _ =>"
        }
        case ForFragment(code) => {
          this << code.pos;
          // TODO indent
          this << "for (" + code + ") {"
        }
        case ImportFragment(code) => {
          this << code.pos;
          this << "import " + code
        }
        case code: EndFragment => {
          this << code.pos;
          // TODO deindent
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

  override def generate(engine: TemplateEngine, uri: String, bindings: List[Binding]): Code = {

    // Load the translation unit
    val content = engine.resourceLoader.load(uri)

    // Determine the package and class name to use for the generated class
    val (packageName, className) = extractPackageAndClassNames(uri)

    // Parse the translation unit
    val fragments = (new SspParser).getPageFragments(content)

    checkSyntax(fragments)

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
      // TODO add the name for better debugging...
      throw new InvalidSyntaxException("Missing #end", f.pos)
    }
  }
}

