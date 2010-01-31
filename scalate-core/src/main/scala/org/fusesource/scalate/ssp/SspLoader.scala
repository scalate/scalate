/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
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
import org.fusesource.scalate.util._
import java.io.File
import java.io.InputStreamReader
import java.io.StringWriter
import java.util.regex.Matcher
import java.util.regex.Pattern
import scala.collection.mutable.HashSet


private object SspLoader
{
  val INCLUDE_REGEX = Pattern.compile( "<%@ +include +file *= *\"([^\"]+)\" *%>", Pattern.MULTILINE | Pattern.DOTALL )
}


class SspLoader
{

  class TranslationUnit( val content: String, val dependencies: Set[String] )
  
  val pageFileEncoding = "UTF-8"


  def loadTranslationUnit(engine:TemplateEngine, uri: String): TranslationUnit = {
    val buffer = new StringBuffer
    val dependencies = new HashSet[String]

    load( engine, uri, buffer, dependencies, Set.empty[String] )

    new TranslationUnit( buffer.toString, Set.empty[String] ++ dependencies )
  }


  private def load( engine:TemplateEngine, uri: String, buffer: StringBuffer, dependencies: HashSet[String],
                    currentlyProcessing: Set[String] ): Unit =
  {
    // Check for cyclical inclusion
    if( currentlyProcessing.contains( uri ) )
      throw new ServerPageException( "Cyclical inclusion of [" + uri + "]" )

    // Record the dependency on this URI
    dependencies += uri

    // Load the contents of the referenced file
    val content = engine.resourceLoader.load(uri)

    // Process the file's contents, including any include directives
    val matcher = SspLoader.INCLUDE_REGEX.matcher( content )
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
