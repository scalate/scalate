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
package org.fusesource.scalate.page

import org.fusesource.scalate._
import org.fusesource.scalate.filter.{ Pipeline, Filter }
import org.fusesource.scalate.support.{ Text, ScalaParseSupport }
import util.IOUtil
import IOUtil._

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import org.yaml.snakeyaml.Yaml
import scala.util.parsing.input.{ NoPosition, CharSequenceReader }
import collection.JavaConverters._

case class Attribute(
  key: Text,
  value: Text)

case class PagePart(
  attributes: List[Attribute],
  content: Text) {

  def attribute(name: String) = attributes.find(_.key.value == name).map(_.value)
  def name = attribute("name")

  def pipeline = attribute("pipeline")

  def filter(engine: TemplateEngine) = Pipeline(pipeline.map(_.value).getOrElse("ssp,markdown").split(",").map { fn =>
    engine.filter(fn) match {
      case Some(filter) => filter
      case _ =>
        throw new InvalidSyntaxException("Invalid filter name: " + fn, pipeline.map(_.pos).getOrElse(NoPosition))
    }
  }.toList)

  def render(context: RenderContext) = filter(context.engine).filter(context, content.value)
}

case class Page(
  context: RenderContext,
  file: Option[File],
  headers: Map[String, AnyRef],
  parts: Map[String, PagePart]) extends Node {

  protected lazy val fileNode = file.map(new FileNode(_))

  override def toString = "Page(" + file + ")"

  def title = headers.get("title") match {
    case Some(t) => t.toString
    case _ => fileNode.map(_.title).getOrElse("")
  }

  def author = headers.getOrElse("author", "").toString

  def createdAt = headers.get("created_at") match {
    case Some(t) => PageFilter.dateFormat.parse(t.toString)
    case _ => fileNode.map(_.createdAt).getOrElse(new Date())
  }

  var link: String = _

  def content(part: String = "content") = parts.get(part).map(_.content.value).getOrElse("")

  def render(part: String = "content") = {
    context.withAttributes(headers) {
      parts.get(part).map(_.render(context)).getOrElse("")
    }
  }
}

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object PageFilter extends Filter with TemplateEngineAddOn {

  val dateFormat = new SimpleDateFormat("yyyy-MM-d HH:mm:ss Z")

  /**
   * Parser for the Page Format
   */
  class PageParser extends ScalaParseSupport {
    override def skipWhitespace = false

    val eof = text("""\z""".r)
    val nl = text("""\r?\n""".r)
    val space = text("""[ \t]+""".r)
    val opt_space = text("""[ \t]*""".r)

    val attribute_key = text("""[a-zA-Z0-9_$-]+""".r)
    val attribute_value = text("""[^ \t\r\n]+""".r)
    val attribute = attribute_key ~ (":" ~> attribute_value) ^^ { case x ~ y => Attribute(x, y) }

    val div = """---""" ~> rep(space ~> attribute) <~ opt_space ~ (nl | eof)

    val content: Parser[Text] =
      guarded(eof, success(Text(""))) |
        guarded(div, success(Text(""))) |
        upto(nl ~ div | nl ~ "\\---" ~ (eof | """\s""".r)) ~ opt(nl) ~ opt("\\---" ~> content) ^^ {
          case x ~ Some(y) ~ None => x + y
          case x ~ None ~ None => x
          case x ~ Some(y) ~ Some(z) => x + y + "---" + z
          case x ~ None ~ Some(z) => x + "---" + z
        }

    val page_part = opt(nl) ~> div ~ content ^^ { case x ~ y => PagePart(x, y) }

    val page_parts =
      guarded(div, rep(page_part)) |
        content ~ rep(page_part) ^^ { case x ~ y => PagePart(Nil, x) :: y }

    def parsePageParts(in: String): List[PagePart] = {
      phraseOrFail(page_parts, in)
    }

    private def phraseOrFail[T](p: Parser[T], in: String): T = {
      val x = phrase(p)(new CharSequenceReader(in))
      x match {
        case Success(result, _) => result
        case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
      }
    }
  }

  val default_name = "content"

  def filter(context: RenderContext, content: String) = {
    val page = parse(context, content)
    page.headers.foreach {
      case (name, value) =>
        context.attributes(name) = value
    }
    var rc = ""
    page.parts.foreach {
      case (name, part) =>
        if (name != default_name) {
          context.attributes(name) = part.render(context)
        } else {
          rc = part.render(context)
        }
    }
    rc
  }

  def parse(context: RenderContext, content: String): Page =
    parse(context, content, None)

  def parse(context: RenderContext, file: File): Page =
    parse(context, file.text, Some(file))

  protected def parse(context: RenderContext, content: String, file: Option[File]): Page = {
    val p = new PageParser
    var parts = p.parsePageParts(content)

    var headers = Map[String, AnyRef]()
    meta_data(context, parts).foreach { meta_data =>
      meta_data.foreach {
        case (key, value) =>
          headers += key -> value
      }
      parts = parts.drop(1)
    }

    var page_parts = Map[String, PagePart]()
    parts.foreach { part =>
      val name = part.name.map(_.value).getOrElse(default_name)
      if (page_parts.contains(name)) {
        throw new InvalidSyntaxException("A page part named: %s was already defined.".format(name), part.name.map(_.pos).getOrElse(NoPosition))
      }
      page_parts += name -> part
    }
    Page(context, file, headers, page_parts)
  }

  def meta_data(context: RenderContext, parts: List[PagePart]) = {
    parts.headOption match {
      case Some(PagePart(Nil, content)) =>
        val yaml = new Yaml();
        val data = context.engine.filter("ssp").map(_.filter(context, content.value)).getOrElse(content.value)
        val result = yaml.load(data).asInstanceOf[java.util.Map[String, AnyRef]].asScala
        Option(result)
      case _ =>
        None
    }
  }

  def apply(te: TemplateEngine) = {
    te.filters += "page" -> PageFilter
    te.pipelines += "page" -> List(PageFilter)
  }

  def main(args: Array[String]) = {
    val in = IOUtil.loadTextFile(new File(args(0)))
    val p = new PageParser
    val x = p.parsePageParts(in)
    println("=========================")
    println(x)
  }

}
