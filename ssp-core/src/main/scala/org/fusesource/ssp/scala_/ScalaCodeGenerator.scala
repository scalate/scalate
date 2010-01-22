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

package org.fusesource.ssp.scala_

import org.fusesource.ssp._
import org.fusesource.ssp.util._
import java.io.File
import java.net.URI
import java.util.regex.Pattern

import scala.util.parsing.combinator._
import java.io.Reader

sealed abstract case class PageFragment()
case class CommentFragment(comment: String) extends PageFragment
case class DollarExpressionFragment(code: String) extends PageFragment
case class ExpressionFragment(code: String) extends PageFragment
case class ScriptletFragment(code: String) extends PageFragment
case class TextFragment(text: String) extends PageFragment

case class AttributeFragment(name: String, className: String, defaultValue: Option[String]) extends PageFragment {
  def isScala28 = false

  def methodArgumentCode = name + ": " + className + (if (isScala28) {
    if (defaultValue.isEmpty) {""} else {" = " + defaultValue.get}
  } else {""})

  def valueCode = "val " + name + " = " + (defaultValue match {
    case Some(expression) => "attributeOrElse[" + className + "](\"" + name + "\", " + expression + ")"
    case None => "attribute[" + className + "](\"" + name + "\")"
  })
}


class SspParser extends JavaTokenParsers {
  def parse(in: Reader) = {
    parseAll(lines, in)
  }

  def parse(in: CharSequence) = {
    parseAll(lines, in)
  }

  def lines = rep(commentFragment | declarationFragment | expressionFragment | scriptletFragment | dollarExpressionFragment | textFragment) ^^ {
    case lines => lines
  }

  def textFragment = upToSpecialCharacter ^^ {
    case a => TextFragment(a.toString)
  }

  def dollarExpressionFragment = parser("${", "}", {DollarExpressionFragment(_)})

  def commentFragment = parser("<%--", "--%>", {CommentFragment(_)})

  def declarationFragment = parser("<%@", "%>", {CommentFragment(_)})

  def expressionFragment = parser("<%=", "%>", {ExpressionFragment(_)})

  def scriptletFragment = parser("<%", "%>", {ScriptletFragment(_)})

  def parser(prefix: String, postfix: String, transform: String => PageFragment) = {
    //val filler = """(.|\n|\r)+"""
    val filler = """.+"""
    val regex = (regexEscape(prefix) + filler + regexEscape(postfix)).r
    regex ^^ {
      case r => val text = r.toString
      val remaining = text.substring(prefix.length, text.length - postfix.length)
      transform(remaining)
    }
  }


  def regexEscape(text: String) = text.mkString("\\", "\\", "")


  def code = chunkOfText

  def any = chunkOfText

  //def chunkOfText = """.[^\<\$\%\-\}]*""".r
  def upToSpecialCharacter = """.[^\<\$\%\-\}]*""".r

  def chunkOfText = """.+""".r

  def token = """[a-zA-Z0-9\$_]+""".r

  def lessThanPercent = """\<\%""".r

  def percentGreaterThan = """\%\>""".r
}


private object ScalaCodeGenerator
{
  private val PATTERN_FLAGS = Pattern.MULTILINE | Pattern.DOTALL

  class TokenInfo(val startTokenRegex: String, val endTokenRegex: String, val payloadParser: (String) => PageFragment)

  val TOKEN_INFO =
  Map("<%" -> new TokenInfo("<%", "%>", {payload: String => ScriptletFragment(payload)}),
    "<%=" -> new TokenInfo("<%=", "%>", {payload: String => ExpressionFragment(payload)}),
    "<%--" -> new TokenInfo("<%--", "--%>", {payload: String => CommentFragment(payload)}),
    "<%@" -> new TokenInfo("<%@", "%>", {payload: String => parseDeclaration(payload)}),
    "${" -> new TokenInfo("${", "}", {payload: String => DollarExpressionFragment(payload)}))

  val FIND_END_TOKEN_REGEX_MAP =
  TOKEN_INFO.transform {(k, v) => Pattern.compile("^(.*?)(" + Pattern.quote(v.endTokenRegex) + ").*$", PATTERN_FLAGS)}

  val FIND_START_TOKEN_REGEX =
  Pattern.compile("^(.*?)(" + (TOKEN_INFO.values.map {info => "(" + Pattern.quote(info.startTokenRegex) + ")"}).mkString("|") + "){1}+.*$",
    PATTERN_FLAGS)

  val SPLIT_ON_LAST_SLASH_REGEX = Pattern.compile("^(.*)/([^/]*)$")

  val PACKAGE =
  """package _PACKAGENAME_""" + "\n\n"


  def parseDeclaration(payload: String): PageFragment = {
    val parser = new DeclarationParser()
    parser.parse(payload) match {
      case parser.Success(fragment: PageFragment, _) =>
        fragment

      case e =>
        throw new ServerPageException("Could not parse declaration: " + e)
    }
  }

  class DeclarationParser extends JavaTokenParsers {
    def parse(in: CharSequence) = {
      parseAll(param, in)
    }

    def param = ("attribute" ~> identifier) ~ (":" ~> typeName) ~ (opt("=" ~> any)) ^^ {
      case i ~ t ~ a => AttributeFragment(i.toString, t.toString, a)
    }

    def identifier = """[a-zA-Z0-9\$_]+""".r

    def typeName = """[a-zA-Z0-9\$_\[\]\.]+""".r

    def any = """.+""".r
  }
}


class ScalaCodeGenerator extends CodeGenerator
{
  override def generateCode(translationUnit: String, outputDirectory: File, uri: String): String = {
    // Determine the package and class name to use for the generated class
    val (packageName, className) = buildPackageAndClassNames(uri)

    // Parse the translation unit
    val fragments = parseFragments(translationUnit)

    // Convert the parsed representation to Scala source code
    val params = fragments.flatMap {
      case p: AttributeFragment => p :: Nil
      case _ => Nil
    }

    val sourceCode = (if (packageName != "") ScalaCodeGenerator.PACKAGE.replaceAll("_PACKAGENAME_", packageName) else "") + """
/** NOTE this file is autogenerated by SSP : see http://ssp.fusesource.org/ */

import org.fusesource.ssp.{Page, PageContext}
import javax.servlet.http._

class """ + className + """ extends Page {

  def render(pageContext: PageContext""" + generateParameterList(params) + """): Unit = {
    import pageContext._

""" + generateCode(fragments) + """

    pageContext.completed
  }
""" + generateRenderMethodWithNoParams(params) + """
}
"""

    // Dump the generated source code to the working directory if requested to do so
    IOUtil.writeBinaryFile(new File(outputDirectory, className + ".scala").toString, sourceCode.getBytes("UTF-8"))

    sourceCode
  }

  private def generateParameterList(params: List[AttributeFragment]) = {
    if (params.isEmpty) {
      ""
    } else {
      params.map(_.methodArgumentCode).mkString(", ", ", ", "")
    }
  }

  private def generateRenderMethodWithNoParams(params: List[AttributeFragment]) = {
    if (params.isEmpty) {
      ""
    } else {
      """
  def render(pageContext: PageContext): Unit = {
    import pageContext._
    """ + params.map {_.valueCode}.mkString("\n    ") + """

    render(pageContext, """ + params.map {_.name}.mkString(", ") + """)
  }
"""
    }
  }

  private def generateCode(fragments: Seq[PageFragment]): StringBuffer =
    fragments.foldLeft(new StringBuffer)((buffer, fragment) => buffer.append(generateCode(fragment)))


  private def generateCode(fragment: PageFragment): String = {
    "\n    " +
            (
                    fragment match {
                      case CommentFragment(code) => ""
                      case DollarExpressionFragment(code) => "pageContext.writeXmlEscape( " + code + " )"
                      case ExpressionFragment(code) => "pageContext.write( " + code + " )"
                      case ScriptletFragment(code) => code
                      case TextFragment(text) => "out.write( \"" + renderText(text) + "\" )"
                      case AttributeFragment(name, className, expression) => ""
                    }
                    ) +
            "\n"
  }


  def parseFragments(translationUnit: String) = parse(translationUnit, List()).reverse

  def buildClassName(uri: String): String = {
    // Determine the package and class name to use for the generated class
    val (packageName, className) = buildPackageAndClassNames(uri)

    // Build the complete class name (including the package name, if any)
    if (packageName == null || packageName.length == 0)
      className
    else
      packageName + "." + className
  }


  private def buildPackageAndClassNames(uri: String): (String, String) = {
    // Normalize the URI (e.g., convert /a/b/../c/foo.ssp to /a/c/foo.ssp)
    val normalizedURI = new URI(uri).normalize
    val matcher = ScalaCodeGenerator.SPLIT_ON_LAST_SLASH_REGEX.matcher(normalizedURI.toString)
    if (matcher.matches == false) throw new ServerPageException("Internal error: unparseable URI [" + uri + "]")

    // Construct package & class names e.g.,
    //   /foo/public/123.ssp --> package "foo.public._serverpages" and classname "_123_ssp"
    //
    // if we have a real class name in the path (such as with JAXRS and implicit views then we munge like this
    //  /foo/bar/SomeClass/index.ssp -> package "foo.bar._serverpages.SomeClass and class _index_ssp
    //
    val firstAttempt = matcher.group(1).replaceAll("[^A-Za-z0-9_/]", "_").replaceAll("/", ".").replaceFirst("\\.([A-Z])", "._serverpages.$1").replaceFirst("^\\.", "")
    val packageName = if (firstAttempt.contains("._serverpages")) {
      firstAttempt
    } else {
      val prefix = if (firstAttempt.length == 0) {""} else {"."}
      firstAttempt + prefix + "_serverpages"
    }

    /**old way
    // Construct package & class names (e.g., /foo/public/123.ssp --> package "_foo._public" and classname "_123_ssp").
    val packageName = matcher.group( 1 ).replaceAll( "[^A-Za-z0-9_/]", "_" ).replaceAll( "^/", "_" ).replaceAll( "/", "._" )
     */
    val className = "_" + matcher.group(2).replace('.', '_')

    (packageName, className)
  }



  // TODO we should really recreate this parser using the Scala parser combinators!
  private def parse(pageContent: CharSequence, fragments: List[PageFragment]): List[PageFragment] = {
    if (pageContent.length > 0) {

      val matcher = ScalaCodeGenerator.FIND_START_TOKEN_REGEX.matcher(pageContent)
      if (matcher.matches) {
        // Determine which start token we've just found, and the text that precedes it.
        // (There must be a better way to determine the start token... but not sure why
        // the regex matches <% instead of <%= even in greedy mode.)
        val beforeToken = matcher.group(1)
        //val startToken = matcher.group( 2 )
        val startToken = findLongestTokenStartingHere(pageContent.toString, matcher.end(1))

        // Find the closing %> or --%> or whatnot that matches the start token we've just found
        val payloadStartIndex = beforeToken.length + startToken.length
        val (endTokenIndex, endToken) = findEndToken(pageContent.subSequence(payloadStartIndex, pageContent.length), startToken) match {
          case Some((index, token)) => (payloadStartIndex + index, token)
          case None => throw new ServerPageException("Start tag [" + startToken + "] has no matching end tag")
        }

        // Extract & parse the content between the start and end tokens (e.g., the "___" in "<%= ___ %>")
        val payload = pageContent.subSequence(payloadStartIndex, endTokenIndex)
        val payloadParser = ScalaCodeGenerator.TOKEN_INFO(startToken).payloadParser
        val payloadFragment = payloadParser(payload.toString)

        // Parse the rest of the content (i.e., whatever comes after the end token)
        val remainderOffset = endTokenIndex + endToken.length
        val remainder = pageContent.subSequence(remainderOffset, pageContent.length)
        parse(remainder, payloadFragment :: parseTextFragment(beforeToken) :: fragments)
      } else {
        parseTextFragment(pageContent.toString) :: fragments
      }
    } else
      fragments
  }


  private def findLongestTokenStartingHere(pageContent: String, startIndex: Int): String = {
    ScalaCodeGenerator.TOKEN_INFO.keys.foldLeft("")((acc, token) => {
      if (token.length > acc.length && pageContent.indexOf(token, startIndex) == startIndex)
        token
      else
        acc
    })
  }


  private def findEndToken(pageContent: CharSequence, startToken: String): Option[(Int, String)] = {
    val matcher = ScalaCodeGenerator.FIND_END_TOKEN_REGEX_MAP(startToken).matcher(pageContent)
    if (matcher.matches)
      Some((matcher.end(1), matcher.group(2)))
    else
      None
  }


  private def parseTextFragment(content: String): PageFragment = content match {
  //case "" => CommentFragment("")
    case s: String => TextFragment(s)
  }


  private def renderText(text: String): StringBuffer =
    text.foldLeft(new StringBuffer)((buffer, c) => {renderChar(buffer, c); buffer})


  private def renderChar(buffer: StringBuffer, c: Char): Unit = {
    if ((c >= '#' && c <= '~') || c == ' ' || c == '!')
      buffer.append(c)
    else if (c == '"')
      buffer.append("\\\"")
    else {
      buffer.append("\\u")
      buffer.append(leftPad(java.lang.Integer.toHexString(c)))
    }
  }


  private def leftPad(s: String): String =
    if (s.length < 4)
      leftPad("0" + s)
    else
      s

}
