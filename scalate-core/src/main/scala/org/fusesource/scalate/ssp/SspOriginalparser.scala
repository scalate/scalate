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
import java.net.URI
import java.util.regex.Pattern

import scala.util.parsing.combinator._
import scala.util.matching.Regex

object SspOriginalParser
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
  Pattern.compile("^(.*?)(" + (TOKEN_INFO.valuesIterator.map {info => "(" + Pattern.quote(info.startTokenRegex) + ")"}).mkString("|") + "){1}+.*$",
    PATTERN_FLAGS)

  val SPLIT_ON_LAST_SLASH_REGEX = Pattern.compile("^(.*)/([^/]*)$")


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

  def parseFragments(translationUnit: String) = parse(translationUnit, List()).reverse

  // TODO we should really recreate this parser using the Scala parser combinators!
  private def parse(pageContent: CharSequence, fragments: List[PageFragment]): List[PageFragment] = {
    if (pageContent.length > 0) {

      val matcher = FIND_START_TOKEN_REGEX.matcher(pageContent)
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
        val payloadParser = TOKEN_INFO(startToken).payloadParser
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
    TOKEN_INFO.keysIterator.foldLeft("")((acc, token) => {
      if (token.length > acc.length && pageContent.indexOf(token, startIndex) == startIndex)
        token
      else
        acc
    })
  }


  private def findEndToken(pageContent: CharSequence, startToken: String): Option[(Int, String)] = {
    val matcher = FIND_END_TOKEN_REGEX_MAP(startToken).matcher(pageContent)
    if (matcher.matches)
      Some((matcher.end(1), matcher.group(2)))
    else
      None
  }


  private def parseTextFragment(content: String): PageFragment = content match {
  //case "" => CommentFragment("")
    case s: String => TextFragment(s)
  }

}
