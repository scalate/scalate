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
package org.fusesource.scalate
package filter

import java.util.concurrent.atomic.AtomicBoolean
import javax.script.ScriptException
import org.fusesource.scalate.support.{ CompilerException, RenderHelper }
import slogging.StrictLogging
import tv.cntt.rhinocoffeescript.Compiler

/**
 * Surrounds the filtered text with &lt;script&gt; and CDATA tags.
 *
 * <p>Useful for including inline Javascript.</p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object CoffeeScriptFilter extends Filter with StrictLogging {

  /**
   * Server side compilation of coffeescript is enabled by default. Disable this flag
   * if you want to disable it (for example to avoid the optional dependency on rhino)
   */
  val serverSideCompile = true
  protected val warnedMissingRhino = new AtomicBoolean()

  def filter(context: RenderContext, content: String) = {

    def clientSideCompile: String = {
      context.attributes("REQUIRES_COFFEE_SCRIPT_JS") = "true"
      """<script type='text/coffeescript'>
         |  //<![CDATA[
         |    """.stripMargin + RenderHelper.indent("    ", content) + """
         |  //]]>
         |</script>""".stripMargin
    }

    def missingRhino(e: Throwable): String = {
      // we don't have rhino on the classpath
      // so lets do client side compilation
      if (warnedMissingRhino.compareAndSet(false, true)) {
        logger.warn("No Rhino on the classpath: " + e + ". Using client side CoffeeScript compile", e)
      }
      clientSideCompile
    }

    if (serverSideCompile) {
      try {
        CoffeeScriptCompiler.compile(content, Some(context.currentTemplate)).fold({
          error =>
            logger.warn("Could not compile coffeescript: " + error, error)
            throw new CompilerException(error.message, Nil)
        }, {
          coffee =>
            """<script type='text/javascript'>
            |  //<![CDATA[
            |    """.stripMargin + RenderHelper.indent("    ", coffee) + """
            |  //]]>
            |</script>""".stripMargin
        })
      } catch {
        case e: NoClassDefFoundError => missingRhino(e)
        case e: ClassNotFoundException => missingRhino(e)
      }
    } else {
      clientSideCompile
    }
  }
}

/**
 * Compiles a .coffee file into JS on the server side
 */
object CoffeeScriptPipeline extends Filter with StrictLogging {

  /**
   * Installs the coffeescript pipeline
   */
  def apply(engine: TemplateEngine): Unit = {
    engine.pipelines += "coffee" -> List(NoLayoutFilter(this, "text/javascript"))
    engine.templateExtensionsFor("js") += "coffee"
  }

  def filter(context: RenderContext, content: String) = {
    CoffeeScriptCompiler.compile(content, Some(context.currentTemplate)).fold({
      error =>
        logger.warn("Could not compile coffeescript: " + error, error)
        throw new CompilerException(error.message, Nil)
    }, {
      coffee => coffee
    })
  }
}

/**
 * A Scala / Rhino Coffeescript compiler.
 */
object CoffeeScriptCompiler {

  /**
   * Compiles a string of Coffeescript code to Javascript.
   *
   * @param code the Coffeescript code
   * @param sourceName a descriptive name for the code unit under compilation (e.g a filename)
   * @param bare if true, no function wrapper will be generated
   * @return the compiled Javascript code
   */
  def compile(code: String, sourceName: Option[String] = None): Either[CompilationError, String] =
    {
      try {
        Right(Compiler.compile(code))
      } catch {
        case e: ScriptException =>
          val line = e.getLineNumber
          val column = e.getColumnNumber
          val message = "CoffeeScript syntax error at %d:%d".format(line, column)
          Left(CompilationError(sourceName, message))
      }
    }
}

case class CompilationError(sourceName: Option[String], message: String)
