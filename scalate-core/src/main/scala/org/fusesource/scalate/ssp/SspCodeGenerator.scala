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

class SspCodeGenerator  extends AbstractCodeGenerator[PageFragment] {

  private class SourceBuilder extends AbstractSourceBuilder[PageFragment] {

    def generate(fragments: List[PageFragment]):Unit = {
      fragments.foreach{ generate(_) }
    }

    def generate(fragment: PageFragment):Unit = {
      fragment match {
        case CommentFragment(code) => {
        }
        case ScriptletFragment(code) => {
          this << code
        }
        case TextFragment(text) => {
          this << "$_scalate_$_context << ( " + asString(text) + " );"
        }
        case af:AttributeFragment => {
        }
        case DollarExpressionFragment(code) => {
          this << "$_scalate_$_context <<< "+code+""
        }
        case ExpressionFragment(code) => {
          this << "$_scalate_$_context << "+code+""
        }
      }
    }

  }

  class Document( val content: String, val dependencies: Set[String] )

  private object DocumentLoader
  {
    val INCLUDE_REGEX = Pattern.compile( "<%@ +include +file *= *\"([^\"]+)\" *%>", Pattern.MULTILINE | Pattern.DOTALL )
  }

  private class DocumentLoader
  {

    val pageFileEncoding = "UTF-8"


    def load(engine:TemplateEngine, uri: String): Document = {
      val buffer = new StringBuffer
      val dependencies = new HashSet[String]

      load( engine, uri, buffer, dependencies, Set.empty[String] )

      new Document( buffer.toString, Set.empty[String] ++ dependencies )
    }


    private def load( engine:TemplateEngine, uri: String, buffer: StringBuffer, dependencies: HashSet[String],
                      currentlyProcessing: Set[String] ): Unit =
    {
      // Check for cyclical inclusion
      if( currentlyProcessing.contains( uri ) )
        throw new TemplateException( "Cyclical inclusion of [" + uri + "]" )

      // Record the dependency on this URI
      dependencies += uri

      // Load the contents of the referenced file
      val content = engine.resourceLoader.load(uri)

      // Process the file's contents, including any include directives
      val matcher = DocumentLoader.INCLUDE_REGEX.matcher( content )
      var firstUnmatchedCharIndex = 0
      while( matcher.find ) {
        // Record the include directive and whatever came before it
        val prefix = content.substring( firstUnmatchedCharIndex, matcher.start )
        val includePath = matcher.group( 1 )
        firstUnmatchedCharIndex = matcher.end

        // Append whatever precedes this include directive
        buffer.append( prefix )

        // Load the referenced file (plus anything it includes, recursively)
        val resolvedIncludePath = engine.resourceLoader.resolve(uri, includePath)
        load( engine, resolvedIncludePath, buffer, dependencies, currentlyProcessing + uri )

        firstUnmatchedCharIndex = matcher.end()
      }

      // Append whatever comes after the last include directive, up to the end of the file
      buffer.append( content.substring( firstUnmatchedCharIndex ) )
    }

  }

  override def generate(engine:TemplateEngine, uri:String, bindings:List[Binding]): Code = {

    // Load the translation unit
    val tu = (new DocumentLoader).load(engine, uri)
    val translationUnit = tu.content

    // Determine the package and class name to use for the generated class
    val (packageName, className) = extractPackageAndClassNames(uri)

    // Parse the translation unit
    val fragments = (new SspParser).getPageFragments(translationUnit)

    // Convert the parsed AttributeFragments into Binding objects
    val templateBindings = fragments.flatMap {
      case p: AttributeFragment => List(Binding(p.name, p.className, p.autoImport, p.defaultValue))
      case _ => Nil
    }

    val sb = new SourceBuilder
    sb.generate(packageName, className, bindings:::templateBindings, fragments)

    Code(this.className(uri), sb.code, tu.dependencies + uri)
  }

}

