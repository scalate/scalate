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
package org.fusesource.scalate.page

import org.fusesource.scalate.support.{Text, ScalaParseSupport}
import org.fusesource.scalate.util.IOUtil
import java.io.File
import org.fusesource.scalate.{RenderContext, InvalidSyntaxException, TemplateEngine, TemplateEngineAddOn}
import org.yaml.snakeyaml.Yaml
import collection.mutable.HashMap
import org.fusesource.scalate.filter.{Pipeline, Filter}
import util.parsing.input.{NoPosition, CharSequenceReader}

case class Page(headers:Map[String, AnyRef], parts:Map[String, String])

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object PageFilter extends Filter with TemplateEngineAddOn {

  case class Attribute(key:Text, value:Text)
  case class PagePart(attributes:List[Attribute], content:Text) {
    def attribute(name:String) = attributes.find(_.key.value == name).map(_.value)
    def name = attribute("name")
    def filters = attribute("filters").orElse(attribute("pipeline"))
  }



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
    val attribute = attribute_key ~ ( ":" ~> attribute_value) ^^ { case x~y=> Attribute(x,y) }

    val div = """---""" ~> rep(space ~> attribute ) <~ opt_space ~ (nl | eof)


    val content:Parser[Text] =
      guarded(eof, success(Text(""))) |
      guarded(div, success(Text(""))) |
      upto(nl ~ div | nl~"\\---"~(eof | """\s""".r)) ~ opt(nl) ~ opt("\\---" ~> content) ^^ {
        case x~Some(y)~None => x+y
        case x~None~None => x
        case x~Some(y)~Some(z) => x+y+"---"+z
        case x~None~Some(z) => x+"---"+z
      }

    val page_part = opt(nl) ~> div ~ content ^^ { case x~y=> PagePart(x, y) }

    val page_parts =
      guarded(div, rep(page_part)) |
      content ~ rep(page_part) ^^ { case x~y=> PagePart(Nil, x) :: y }

    def parsePageParts(in: String): List[PagePart] = {
      phraseOrFail(page_parts, in)
    }

    private def phraseOrFail[T](p: Parser[T], in: String): T = {
      var x = phrase(p)(new CharSequenceReader(in))
      x match {
        case Success(result, _) => result
        case NoSuccess(message, next) => throw new InvalidSyntaxException(message, next.pos);
      }
    }
  }

  val default_filters = "ssp,markdown"
  val default_name = "content"

  def filter(context: RenderContext, content: String) = {
    val p = new PageParser
    var parts = p.parsePageParts(content)

    meta_data(context, parts) match {
      case Some(meta_data)=>
        // Set all meta data values attributes
        meta_data.foreach { case (key, value)=>
          context.attributes(key) = value
        }
        parts = parts.drop(1)
      case _ =>
    }

    var rendered_parts = new HashMap[String, String]
    parts.foreach{ part=>

      val name = part.name.map(_.value).getOrElse(default_name)
      val filters = part.filters.map(_.value).getOrElse(default_filters).split(",").map{fn=>
        context.engine.filter(fn) match {
          case Some(filter) => filter
          case _ =>
            throw new InvalidSyntaxException("Invalid filter name: "+fn, part.filters.map(_.pos).getOrElse(NoPosition) )
        }
      }

      val content = new Pipeline(filters.toList).filter(context, part.content.value)

      if( rendered_parts.contains(name) ) {
        throw new InvalidSyntaxException("A page part named: %s was allready defined.".format(name), part.name.map(_.pos).getOrElse(NoPosition) )
      }

      rendered_parts.put(name, content)
    }

    for( (name, content) <- rendered_parts if name!=default_name ) {
      context.attributes(name) = content
    }

    rendered_parts.get(default_name).getOrElse("")
  }

  def parse(context: RenderContext, content: String):Page = {
    val p = new PageParser
    var parts = p.parsePageParts(content)

    var headers = Map[String, AnyRef]()
    meta_data(context, parts).foreach { meta_data=>
      meta_data.foreach { case (key, value)=>
        headers += key->value
      }
      parts = parts.drop(1)
    }

    var page_parts = Map[String, String]()
    parts.foreach{ part=>
      val name = part.name.map(_.value).getOrElse(default_name)
      val value =  part.content.value
      page_parts += name->value
    }
    Page(headers, page_parts)
  }

  def meta_data(context: RenderContext, parts:List[PagePart] ) = {
    parts.headOption match {
      case Some(PagePart(Nil, content))  =>
        val yaml = new Yaml();
        val data = context.engine.filter("ssp").map( _.filter(context, content.value) ).getOrElse(content.value)
        Option(collection.JavaConversions.asMap(yaml.load(data).asInstanceOf[java.util.Map[String, AnyRef]]))
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