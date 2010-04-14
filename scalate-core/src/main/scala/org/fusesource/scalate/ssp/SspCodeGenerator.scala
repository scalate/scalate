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

  implicit def textToString(text:Text) = text.value
  implicit def textOptionToString(text:Option[Text]):Option[String] = text match {
    case None=>None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[PageFragment] {

    protected def isImportStatementOrCommentOrWhitespace(fragment: PageFragment) = fragment match {
      case s: ScriptletFragment if (s.code.trim.startsWith("import ")) => true
      case s: TextFragment if (s.text.trim.length == 0) => true
      case s: CommentFragment => true
      case _ => false
    }

    def generate(fragments: List[PageFragment]):Unit = {
      fragments.foreach(generate)
    }

    def generate(fragment: PageFragment):Unit = {
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
        case af:AttributeFragment => {
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

    // lets check that the syntax is correct
    val endStack = new Stack[PageFragment]
    def expect[T](f: PageFragment, name: String): Unit = if (endStack.isEmpty) {
        throw new InvalidSyntaxException("Missing " + name, f.pos)
      } else {
        if (!endStack.head.isInstanceOf[T]) {
          throw new InvalidSyntaxException("Should be used within " + name + " not " + f.tokenName, f.pos)
        }
      }

    for (f <- fragments) f match {
      case f: IfFragment => endStack.push(f)
      case f: ForFragment => endStack.push(f)
      case f: EndFragment => if (endStack.isEmpty) {
        throw new InvalidSyntaxException("Extra #end without matching #if, #for, #match", f.pos)
      } else {
        endStack.pop
      }
      case f: ElseIfFragment => expect[IfFragment](f, "if")
      case f: ElseFragment => expect[IfFragment](f, "if")

      // TODO check that else within if and no else if after else
      case _ =>
    }
    if (!endStack.isEmpty) {
      val f = endStack.head
      // TODO add the name for better debugging...
      throw new InvalidSyntaxException("Missing #end", f.pos)
    }


    // Convert the parsed AttributeFragments into Binding objects
    val templateBindings = fragments.flatMap {
      case p: AttributeFragment => List(Binding(p.name, p.className, p.autoImport, p.defaultValue))
      case _ => Nil
    }

    val sb = new SourceBuilder
    sb.generate(engine, packageName, className, bindings:::templateBindings, fragments)

    Code(this.className(uri), sb.code, Set(uri), sb.positions)
  }

}

