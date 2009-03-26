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


package com.mh.serverpages.scala_

import com.mh.serverpages._
import com.mh.serverpages.util._
import java.io.File
import java.io.StringWriter
import java.net.URI
import java.util.regex.Matcher
import java.util.regex.Pattern


private sealed abstract case class PageFragment()
private case class EmptyFragment() extends PageFragment
private case class EscapeFragment( code: String ) extends PageFragment
private case class ExpressionFragment( code: String ) extends PageFragment
private case class ScriptletFragment( code: String ) extends PageFragment
private case class TextFragment( text: String ) extends PageFragment


private object ScalaCodeGenerator
{

  private val PATTERN_FLAGS = Pattern.MULTILINE | Pattern.DOTALL

  class TokenInfo( val startTokenRegex: String, val endTokenRegex: String, val payloadParser: (String) => PageFragment )

  val TOKEN_INFO =
      Map( "<%"   -> new TokenInfo( "<%"  , "%>"  , { payload: String => ScriptletFragment( payload )  } ),
           "<%="  -> new TokenInfo( "<%=" , "%>"  , { payload: String => ExpressionFragment( payload ) } ),
           "<%--" -> new TokenInfo( "<%--", "--%>", { payload: String => EmptyFragment()               } ),
           "${"   -> new TokenInfo( "${"  , "}"   , { payload: String => EscapeFragment( payload )     } ) )

  val FIND_END_TOKEN_REGEX_MAP =
      TOKEN_INFO.transform { (k,v) => Pattern.compile( "^(.*?)(" + Pattern.quote( v.endTokenRegex ) + ").*$", PATTERN_FLAGS ) }

  val FIND_START_TOKEN_REGEX =
      Pattern.compile( "^(.*?)(" + ( TOKEN_INFO.values.map { info => "(" + Pattern.quote( info.startTokenRegex ) + ")" } ).mkString( "|" ) + "){1}+.*$",
                       PATTERN_FLAGS )

  val SPLIT_ON_LAST_SLASH_REGEX = Pattern.compile( "^(.*)/([^/]*)$" )

  val PACKAGE =
      """package _PACKAGENAME_""" + "\n\n"

  val CODE_PREFIX =
      """import com.mh.serverpages.util.XmlEscape""" + "\n" +
      """import javax.servlet.http._""" + "\n" +
      "\n" +
      """class _CLASSNAME_ extends HttpServlet {""" + "\n" +
      """  override def service( request: HttpServletRequest, response: HttpServletResponse ): Unit = {""" + "\n" +
      """    val out = new java.io.PrintWriter( new java.io.OutputStreamWriter( response.getOutputStream, "UTF-8" ) )""" + "\n"

  val CODE_SUFFIX =
      "\n" +
      """    out.close""" + "\n" +
      """  }""" + "\n" +
      "\n" +
      """  private def _safeToString( x: Any ): String = x match {""" + "\n" +
      """    case r: AnyRef => if( r == null ) "null" else r.toString""" + "\n" +
      """    case v: AnyVal => v.toString""" + "\n" +
      """    case _ => "null" """ + "\n" +
      """  }""" + "\n" +
      """  """ + "\n" +
      """}"""

}


class ScalaCodeGenerator extends CodeGenerator
{

  override def generateCode( translationUnit: String, outputDirectory: File, uri: String ): String = {
    // Determine the package and class name to use for the generated class
    val (packageName, className) = buildPackageAndClassNames( uri )

    // Parse the translation unit
    val fragments = parse( translationUnit, List() ).reverse

    // Convert the parsed representation to Scala source code
    val sourceCode = ( if( packageName != "" ) ScalaCodeGenerator.PACKAGE.replaceAll( "_PACKAGENAME_", packageName ) else "" ) +
                     ScalaCodeGenerator.CODE_PREFIX.replaceAll( "_CLASSNAME_", className ) +
                     generateCode( fragments ) +
                     ScalaCodeGenerator.CODE_SUFFIX

    // Dump the generated source code to the working directory if requested to do so
    IOUtil.writeBinaryFile( new File( outputDirectory, className + ".scala" ).toString, sourceCode.getBytes( "UTF-8" ) )

    sourceCode
  }


  def buildClassName( uri: String ): String = {
    // Determine the package and class name to use for the generated class
    val (packageName, className) = buildPackageAndClassNames( uri )

    // Build the complete class name (including the package name, if any)
    if( packageName == null )
      className
    else
      packageName + "." + className
  }


  private def buildPackageAndClassNames( uri: String ): (String, String) = {
    // Normalize the URI (e.g., convert /a/b/../c/foo.ssp to /a/c/foo.ssp)
    val normalizedURI = new URI( uri ).normalize
    val matcher = ScalaCodeGenerator.SPLIT_ON_LAST_SLASH_REGEX.matcher( normalizedURI.toString )
    if( matcher.matches == false )  throw new ServerPageException( "Internal error: unparseable URI [" + uri + "]" )

    // Construct package & class names (e.g., /foo/public/123.ssp --> package "_foo._public" and classname "_123_ssp").
    val packageName = matcher.group( 1 ).replaceAll( "[^A-Za-z0-9_/]", "_" ).replaceAll( "^/", "_" ).replaceAll( "/", "._" )
    val className = "_" + matcher.group( 2 ).replace( '.', '_' )

    (packageName, className)
  }


  private def generateCode( fragments: Seq[PageFragment] ): StringBuffer =
    fragments.foldLeft( new StringBuffer )( (buffer, fragment) => buffer.append( generateCode( fragment ) ) )


  private def generateCode( fragment: PageFragment ): String = {
    "\n    " +
    (
      fragment match {
        case EmptyFragment() => ""
        case EscapeFragment(code) => "out.write( XmlEscape.escape( _safeToString( " + code + " ) ) )"
        case ExpressionFragment(code) => "out.write( _safeToString( " + code + " ) )"
        case ScriptletFragment(code) => code
        case TextFragment(text) => "out.write( \"" + renderText( text ) + "\" )"
      }
    ) +
    "\n"
  }


  private def parse( pageContent: CharSequence, fragments: List[PageFragment] ): List[PageFragment] = {
    if( pageContent.length > 0 ) {
      val matcher = ScalaCodeGenerator.FIND_START_TOKEN_REGEX.matcher( pageContent )
      if( matcher.matches ) {
        // Determine which start token we've just found, and the text that precedes it.
        // (There must be a better way to determine the start token... but not sure why
        // the regex matches <% instead of <%= even in greedy mode.)
        val beforeToken = matcher.group( 1 )
        //val startToken = matcher.group( 2 )
        val startToken = findLongestTokenStartingHere( pageContent.toString, matcher.end( 1 ) )

        // Find the closing %> or --%> or whatnot that matches the start token we've just found
        val payloadStartIndex = beforeToken.length + startToken.length
        val (endTokenIndex, endToken) = findEndToken( pageContent.subSequence( payloadStartIndex, pageContent.length ), startToken ) match {
          case Some((index, token)) => (payloadStartIndex + index, token)
          case None => throw new ServerPageException( "Start tag [" + startToken + "] has no matching end tag" )
        }

        // Extract & parse the content between the start and end tokens (e.g., the "___" in "<%= ___ %>")
        val payload = pageContent.subSequence( payloadStartIndex, endTokenIndex )
        val payloadParser = ScalaCodeGenerator.TOKEN_INFO( startToken ).payloadParser
        val payloadFragment = payloadParser( payload.toString )

        // Parse the rest of the content (i.e., whatever comes after the end token)
        val remainderOffset = endTokenIndex + endToken.length
        val remainder = pageContent.subSequence( remainderOffset, pageContent.length )
        parse( remainder, payloadFragment :: parseTextFragment( beforeToken ) :: fragments )
      } else {
        parseTextFragment( pageContent.toString ) :: fragments
      }
    } else
      fragments
  }


  private def findLongestTokenStartingHere( pageContent: String, startIndex: Int ): String = {
    ScalaCodeGenerator.TOKEN_INFO.keys.foldLeft( "" )( (acc, token) => {
          if( token.length > acc.length  &&  pageContent.indexOf( token, startIndex ) == startIndex )
            token
          else
            acc
        } )
  }


  private def findEndToken( pageContent: CharSequence, startToken: String ): Option[(Int, String)] = {
    val matcher = ScalaCodeGenerator.FIND_END_TOKEN_REGEX_MAP( startToken ).matcher( pageContent )
    if( matcher.matches )
      Some( (matcher.end( 1 ), matcher.group( 2 )) )
    else
      None
  }


  private def parseTextFragment( content: String ): PageFragment = content match {
    case "" => EmptyFragment()
    case s: String => TextFragment(s)
  }


  private def renderText( text: String ): StringBuffer =
    text.foldLeft( new StringBuffer )( (buffer,c) => { renderChar( buffer, c ); buffer } )


  private def renderChar( buffer: StringBuffer, c: Char ): Unit = {
    if( ( c >= '#' && c <= '~' )  ||  c == ' '  ||  c == '!' )
      buffer.append( c )
    else if( c == '"' )
      buffer.append( "\\\"" )
    else {
      buffer.append( "\\u" )
      buffer.append( leftPad( java.lang.Integer.toHexString( c ) ) )
    }
  }


  private def leftPad( s: String ): String =
    if( s.length < 4 )
      leftPad( "0" + s )
    else
      s

}
