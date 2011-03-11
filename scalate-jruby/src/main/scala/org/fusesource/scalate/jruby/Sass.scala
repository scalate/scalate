package org.fusesource.scalate.jruby

import org.fusesource.scalate.filter.Filter
import org.fusesource.scalate.util.Log
import org.fusesource.scalate.{TemplateException, RenderContext, TemplateEngine, TemplateEngineAddOn}
import java.io.File
import org.fusesource.scalate.servlet.ServletRenderContext

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object Sass extends TemplateEngineAddOn with Log {

  def apply(te: TemplateEngine) = {
    val jruby = new JRuby(List[File]())
    if( !te.filters.contains("sass") ) {
      val sass = new Sass(jruby, te)
      te.filters += "sass"->sass
      te.pipelines += "sass"->List(NoLayout(sass))
    }

    if( !te.filters.contains("scss") ) {
      val scss = new Scss(jruby, te)
      te.filters += "scss"->scss
      te.pipelines += "scss"->List(NoLayout(scss))
    }
  }


}

case class NoLayout(val next: Sass) extends Filter {
  def filter(context: RenderContext, content: String) = {
    context.attributes("layout")="" // disable the layout
    context match {
      case x:ServletRenderContext => x.response.setContentType("text/css")
      case _ =>
    }
    next.filter(context, content)
  }
}

class Sass(val jruby:JRuby, val engine: TemplateEngine) extends Filter {

  def syntax = "sass"

  def filter(context: RenderContext, content: String) = {

    val sassPaths = List[String]()
    var result: StringBuffer = new StringBuffer
    jruby.put("@result", result)
    jruby.run(
      "require 'haml-3.0.25/lib/sass'",
      "options = {}",
      "options[:load_paths] = ["+sassPaths.map("'"+_+"'").mkString(",")+"]",
      "options[:cache_location] = '"+engine.workingDirectory+"/sass'",
      "options[:style] = " + (if (engine.isDevelopmentMode) ":expanded" else ":compressed") + "",
      "options[:line_comments] = " + (if (engine.isDevelopmentMode) "true" else "false") + "",
      "options[:syntax] = :"+syntax,
      "content = <<SCALATE_SASS_EOF\n"+content+"\nSCALATE_SASS_EOF",
      "tree = ::Sass::Engine.new(content, options).to_tree",
      "@result.append(tree.render)") match {
      case Right(result) => result.toString
      case Left((exception, errors)) =>
        val err1 = """(?m)([a-zA-Z_0-9-]+[.]s[ca]ss:\d+:.+)$""".r
        val err2 = """([(]s[ca]ss[)]:\d+:.+)$""".r
        errors match {
          case err1(msg) => throw new TemplateException(msg, exception)
          case err2(msg) => throw new TemplateException(msg, exception)
        }
        throw new TemplateException(errors, exception)
    }
  }

}

class Scss(jruby:JRuby, engine: TemplateEngine) extends Sass(jruby, engine) {
  override def syntax = "scss"
}