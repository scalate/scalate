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

package org.fusesource.scalate.tool.commands

import java.{util => ju}
import java.io.{File, PrintWriter}
import collection.JavaConversions._
import org.fusesource.scalate.{TemplateEngine, DefaultRenderContext}
import org.fusesource.scalate.tool.Scalate._
import org.fusesource.scalate.tool.CommandRunner
import org.fusesource.scalate.support.FileResourceLoader
import com.beust.jcommander.{Command, Argument, Parameter}

/**
 * The 'scalate run' sub command.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Command(description = "Renders a Scalate template file")
class Run extends CommandRunner {
  @Parameter(names = Array("--root"), description = "Sets the root of the tempalte search path.")
  var root = new File(".")

  @Parameter(names = Array("--workdir"), description = "Sets the work directory where scalate generates class files to. Defaults to a temporary directory.")
  var workdir: File = _

  @Argument(index = 0, description = "Name of the template to render")
  var template: String = _

  @Parameter(description = "arguments", required = true)
  var args: ju.List[String] = new ju.ArrayList[String]

  def commandName = "run"

  def run: Int = {
    render(template, args.toList)
    0
  }

  def render(path: String, args: List[String]): Int = {
    //    val buffer = new StringWriter()
    //    val out = new PrintWriter(buffer)
    try {
      val engine = new TemplateEngine
      if (workdir != null) {
        engine.workingDirectory = workdir
      }
      engine.resourceLoader = new FileResourceLoader(Some(root))

      val writer = new PrintWriter(System.out)
      val context = new DefaultRenderContext(engine, writer);
      context.attributes("args") = args
      context.include(path, true)
      writer.flush
      return 0;

    } catch {
      case e: Exception =>
        error("Could not render: " + path, e)
        error("Due to: " + e, e)
        return -1
    }
  }
}
