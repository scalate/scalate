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
import java.util.regex.Pattern
import scala.collection.mutable.HashSet

class SspCodeGenerator extends AbstractCodeGenerator[PageFragment] {

  implicit def textToString(text:Text) = text.value
  implicit def textOptionToString(text:Option[Text]):Option[String] = text match {
    case None=>None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[PageFragment] {

    def generate(fragments: List[PageFragment]):Unit = {
      // lets find all nodes which are imports first...
      val firstNonImport = fragments.findIndexOf(!isImportStatement(_))
      if (firstNonImport > 0) {
        val (imports, others) = fragments.splitAt(firstNonImport)
        imports.foreach(generate)
        others.foreach(generate)
      }
      else {
        fragments.foreach(generate)
      }
    }

    def isImportStatement(fragment: PageFragment) = fragment match {
      case s: ScriptletFragment if (s.code.trim.startsWith("import ")) => true
      case _ => false
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

  override def generate(engine:TemplateEngine, uri:String, bindings:List[Binding]): Code = {

    // Load the translation unit
    val content = engine.resourceLoader.load(uri)

    // Determine the package and class name to use for the generated class
    val (packageName, className) = extractPackageAndClassNames(uri)

    // Parse the translation unit
    val fragments = (new SspParser).getPageFragments(content)

    // Convert the parsed AttributeFragments into Binding objects
    val templateBindings = fragments.flatMap {
      case p: AttributeFragment => List(Binding(p.name, p.className, p.autoImport, p.defaultValue))
      case _ => Nil
    }

    val sb = new SourceBuilder
    sb.generate(packageName, className, bindings:::templateBindings, fragments)

    Code(this.className(uri), sb.code, Set(uri), sb.positions)
  }

}

