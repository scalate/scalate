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

package org.fusesource.scalate.mustache


import org.fusesource.scalate._
import support.{Code, AbstractCodeGenerator}
import collection.mutable.Stack
import util.Log

object MustacheCodeGenerator extends Log

/**
 * @version $Revision: 1.1 $
 */
class MustacheCodeGenerator extends AbstractCodeGenerator[Statement] {
  import MustacheCodeGenerator._

  override val stratumName = "MSC"

  implicit def textToString(text: Text) = text.value

  implicit def textOptionToString(text: Option[Text]): Option[String] = text match {
    case None => None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[Statement] {
    protected val scopes = new Stack[String]
    protected var scopeIndex = 1

    protected def isImportStatementOrCommentOrWhitespace(fragment: Statement) = fragment match {
      //case s: Text if (s.value.trim.length == 0) => true
      case s: Comment => true
      case _ => false
    }

    def generate(fragments: List[Statement]): Unit = {
      this << "import _root_.org.fusesource.scalate.mustache._"
      this << ""
      
      this << "val " + pushScope + " = " + "Scope($_scalate_$_context)"

      fragments.foreach(generate)
    }

    def generate(fragment: Statement): Unit = {
      fragment match {
        case Comment(code) => {
        }
        case Text(text) => {
          this << fragment.pos;
          this << "$_scalate_$_context << ( " + asString(text) + " )"
        }
        case Variable(name, unescape) => {
          this << fragment.pos;
          this << "" + scope + ".renderVariable(\"" + name + "\", " + unescape + ")"
        }
        case Section(name, body) => {
          this << fragment.pos;
          this << "" + scope + ".section(\"" + name + "\") { " + pushScope + " =>"
          indent {
            body.foreach(generate)
          }
          popScope
          this << "}"
        }
        case InvertSection(name, body) => {
          this << fragment.pos;
          this << "" + scope + ".invertedSection(\"" + name + "\") { " + pushScope + " =>"
          indent {
            body.foreach(generate)
          }
          popScope
          this << "}"
        }
        case Partial(name) => {
          this << fragment.pos;
          this << "" + scope + ".partial(\"" + name + "\")"
        }
        case ImplicitIterator(name) => {
          this << fragment.pos;
          this << "" + scope + ".implicitIterator = Some(\"" + name + "\")"
        }
        case Pragma(name, options) =>
          this << fragment.pos;
          this << "$_scalate_$_context << \"ERROR: This implementation of mustache doesn't understand the '" + name + "' pragma\""
        case SetDelimiter(open,close) =>
        case s => {
          warn("Unsupported statement: %s", s)
        }
      }
    }


    def scope = scopes.head

    protected def pushScope: String = {
      val name = "$_scope_" + scopeIndex
      scopeIndex += 1
      scopes.push(name)
      name
    }

    protected def popScope = scopes.pop
  }

  override def generate(engine: TemplateEngine, source: TemplateSource, bindings: List[Binding]): Code = {

    // Load the translation unit
    val content = source.text
    val uri = source.uri

    // Parse the translation unit
    val fragments = (new MustacheParser).parse(content)

    val sb = new SourceBuilder
    sb.generate(engine, source, bindings, fragments)

    Code(source.className, sb.code, Set(uri), sb.positions)
  }
}

