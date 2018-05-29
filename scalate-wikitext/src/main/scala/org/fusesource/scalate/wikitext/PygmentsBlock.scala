/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.wikitext

import org.eclipse.mylyn.wikitext.core.parser.Attributes
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.AbstractConfluenceDelimitedBlock
import java.lang.String
import collection.mutable.ListBuffer
import org.fusesource.scalate.util.Threads._
import util.parsing.input.CharSequenceReader
import util.parsing.combinator.RegexParsers
import org.fusesource.scalate.support.RenderHelper
import org.fusesource.scalate._
import org.fusesource.scalate.filter.Filter
import java.io.{ File, ByteArrayInputStream, ByteArrayOutputStream }
import util.{ Files, Log, IOUtil }

object Pygmentize extends Log with Filter with TemplateEngineAddOn {

  /**
   * Add the markdown filter to the template engine.
   */
  def apply(te: TemplateEngine) = {
    te.filters += "pygmentize" -> Pygmentize

    // add the imports
    te.importStatements :+= "import org.fusesource.scalate.wikitext.PygmentizeHelpers._"
  }

  def filter(context: RenderContext, content: String): String = {
    pygmentize(content)
  }

  // lets calculate once on startup
  private lazy val _installed: Boolean = {
    try {
      val process = Runtime.getRuntime.exec(Array("pygmentize", "-V"))
      thread("pygmetize err handler") {
        IOUtil.copy(process.getErrorStream, System.err)
      }

      val out = new ByteArrayOutputStream()
      thread("pygmetize out handler") {
        IOUtil.copy(process.getInputStream, out)
      }

      process.waitFor
      if (process.exitValue != 0) {
        false
      } else {
        val output = new String(out.toByteArray).trim
        debug("Pygmentize installed: " + output)
        true
      }
    } catch {
      case e: Exception =>
        debug(e, "Failed to start pygmetize: " + e)
        false
    }
  }

  def isInstalled: Boolean = _installed

  def unindent(data: String): String = unindent(data.split("""\r?\n""").toList)

  def unindent(data: collection.Seq[String]): String = {
    var content = data
    // To support indenting the macro.. we figure out the indent level of the
    // code block by looking at the indent of the last line
    val indent_re = """^([ \t]+)$""".r
    content.lastOption match {
      case Some(indent_re(indent)) =>
        // strip off those indents.
        content = content.map(_.replaceFirst("""^[ \t]{""" + indent.size + """}""", ""))
      case _ =>
    }
    content.mkString("\n")
  }
  object OptionParser extends RegexParsers {

    override def skipWhitespace = false

    val lang = """[\w0-9_-]+""".r
    val key = """[\w0-9_-]+""".r
    val value = """[\w0-9_-]+""".r

    val attributes = repsep(key ~ ("=" ~> value), whiteSpace) ^^ { list =>
      var rc = Map[String, String]()
      for ((x ~ y) <- list) {
        rc += x -> y
      }
      rc
    }

    val option_line: Parser[(Option[String], Map[String, String])] =
      guard(key ~ "=") ~> attributes <~ opt(whiteSpace) ^^ { case y => (None, y) } |
        lang ~ opt(whiteSpace ~> attributes <~ opt(whiteSpace)) ^^ {
          case x ~ Some(y) => (Some(x), y)
          case x ~ None => (Some(x), Map())
        }

    def apply(in: String) = {
      (phrase(opt(whiteSpace) ~> option_line)(new CharSequenceReader(in))) match {
        case Success(result, _) => Some(result)
        //        case NoSuccess(message, next) => throw new Exception(message+" at "+next.pos)
        case NoSuccess(message, next) => None
      }
    }
  }

  def pygmentize(data: String, options: String = ""): String = {

    var lang1 = "text"
    var lines = false
    var wide = false

    val opts = OptionParser(options)

    opts match {
      case Some((lang, atts)) =>
        lang1 = lang.getOrElse(lang1)
        for ((key, value) <- atts) {
          key match {
            case "lines" => lines = java.lang.Boolean.parseBoolean(value)
            case "wide" => wide = java.lang.Boolean.parseBoolean(value)
          }
        }
      case _ =>
    }

    // Now look for header sections...
    val header_re = """(?s)\n------+\s*\n\s*([^:\s]+)\s*:\s*([^\n]+)\n------+\s*\n(.*)""".r

    header_re.findFirstMatchIn("\n" + data) match {
      case Some(m1) =>

        lang1 = m1.group(1)
        val title1 = m1.group(2)
        var data1 = m1.group(3)

        header_re.findFirstMatchIn(data1) match {
          case Some(m2) =>

            data1 = data1.substring(0, m2.start)

            val lang2 = m2.group(1)
            val title2 = m2.group(2)
            val data2 = m2.group(3)

            val colored1 = pygmentize(data1, lang1, lines)
            val colored2 = pygmentize(data2, lang2, lines)

            var rc = """<div class="compare"><div class="compare-left"><h3>%s</h3><div class="syntax">%s</div></div><div class="compare-right"><h3>%s</h3><div class="syntax">%s</div></div><br class="clear"/></div>
              |""".stripMargin.format(title1, colored1, title2, colored2)

            if (wide) {
              rc = """<div class="wide">%s</div>""".format(rc)
            }
            rc

          case None =>
            """<div class="compare"><h3>%s</h3><div class="syntax">%s</div></div>
              |""".stripMargin.format(title1, pygmentize(data1, lang1, lines))

        }
      case None =>
        """<div class="syntax">%s</div>
          |""".stripMargin.format(pygmentize(data, lang1, lines))
    }
  }

  def pygmentize(body: String, lang: String, lines: Boolean): String = {
    if (!isInstalled) {
      "<pre name='code' class='brush: " + lang + "; gutter: " + lines + ";'><code>" + RenderHelper.sanitize(body) + "</code></pre>"
    } else {
      var options = "style=colorful"
      if (lines) {
        options += ",linenos=1"
      }

      val process = Runtime.getRuntime.exec(Array("pygmentize", "-O", options, "-f", "html", "-l", lang))

      thread("pygmetize err handler") {
        IOUtil.copy(process.getErrorStream, System.err)
      }

      thread("pygmetize in handler") {
        IOUtil.copy(new ByteArrayInputStream(body.getBytes), process.getOutputStream)
        process.getOutputStream.close
      }

      val out = new ByteArrayOutputStream()
      IOUtil.copy(process.getInputStream, out)
      process.waitFor
      if (process.exitValue != 0) {
        throw new RuntimeException("'pygmentize' execution failed: %d.  Did you install it from http://pygments.org/download/ ?".format(process.exitValue))
      }

      new String(out.toByteArray).replaceAll("""\r?\n""", "&#x000A;")
    }
  }

}

/**
 * View helper methods for use inside templates
 */
object PygmentizeHelpers {

  // TODO add the text version and the macro version......
  // TODO is there a simpler polymophic way to write functions like this
  // that operate on text content from a String, block, URI, File, Resource etc...

  def pygmentizeFile(file: File, lang: String = "", lines: Boolean = false): String = {
    val content = IOUtil.loadTextFile(file)
    val defaultLang = getOrUseExtension(lang, file.toString)
    Pygmentize.pygmentize(content, defaultLang)
  }

  def pygmentizeUri(uri: String, lang: String = "", lines: Boolean = false)(implicit resourceContext: RenderContext): String = {
    val content = resourceContext.load(uri)
    val defaultLang = getOrUseExtension(lang, uri)
    Pygmentize.pygmentize(content, defaultLang)
  }

  protected def getOrUseExtension(lang: String, uri: String): String = {
    if (lang.isEmpty) {
      Files.extension(uri)
    } else {
      lang
    }
  }
}

class PygmentsBlock extends AbstractConfluenceDelimitedBlock("pygmentize") {

  var language: String = _
  var lines: Boolean = false

  var content = ListBuffer[String]()

  override def beginBlock() = {
    val attributes = new Attributes();
    attributes.setCssClass("syntax");
    builder.beginBlock(BlockType.DIV, attributes);
  }

  override def handleBlockContent(value: String) = {
    // collect all the content lines..
    content += value
  }

  override def endBlock() = {
    import Pygmentize._
    builder.charactersUnescaped(pygmentize(unindent(content), language, lines))
    content.clear
    builder.endBlock();
  }

  override def setOption(option: String) = {
    language = option.toLowerCase();
  }

  override def setOption(key: String, value: String) = {
    key match {
      case "lines" => lines = value == "true"
      case "lang" => language = value
    }
  }

}
