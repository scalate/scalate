package org.fusesource.scalate.jruby

import org.fusesource.scalate.util.Log
import org.fusesource.scalate.{ TemplateException, RenderContext, TemplateEngine, TemplateEngineAddOn }
import org.fusesource.scalate.filter.{ NoLayoutFilter, CssFilter, Pipeline, Filter }

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object Sass extends TemplateEngineAddOn with Log {

  def apply(te: TemplateEngine) = {
    val jruby = new JRuby
    if (!te.filters.contains("sass")) {
      val sass = new Sass(jruby, te)
      te.filters += "sass" -> Pipeline(List(sass, CssFilter))
      te.pipelines += "sass" -> List(NoLayoutFilter(sass, "text/css"))
      te.templateExtensionsFor("css") += "sass"
    }

    if (!te.filters.contains("scss")) {
      val scss = new Scss(jruby, te)
      te.filters += "scss" -> Pipeline(List(scss, CssFilter))
      te.pipelines += "scss" -> List(NoLayoutFilter(scss, "text/css"))
      te.templateExtensionsFor("css") += "scss"
    }
  }
}

class Sass(val jruby: JRuby, val engine: TemplateEngine) extends Filter {

  def syntax = "sass"

  def filter(context: RenderContext, content: String) = {

    val result: StringBuffer = new StringBuffer
    jruby.put("@result", result)
    jruby.run(
      "require 'haml-3.0.25/lib/sass'",
      "options = {}",
      "options[:load_paths] = [" + context.engine.sourceDirectories.map("'" + _ + "'").mkString(",") + "]",
      "options[:cache_location] = '" + engine.workingDirectory + "/sass'",
      "options[:style] = " + (if (engine.isDevelopmentMode) ":expanded" else ":compressed") + "",
      "options[:line_comments] = " + (if (engine.isDevelopmentMode) "true" else "false") + "",
      "options[:syntax] = :" + syntax,
      "content = <<SCALATE_SASS_EOF\n" + content + "\nSCALATE_SASS_EOF",
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

class Scss(jruby: JRuby, engine: TemplateEngine) extends Sass(jruby, engine) {
  override def syntax = "scss"
}
