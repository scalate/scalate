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
package tool.commands

import java.{util => ju, lang => jl}
import java.io.File
import collection.JavaConversions._
import org.apache.felix.gogo.commands.{Action, Option => option, Argument => argument, Command => command}
import org.osgi.service.command.CommandSession


/**
 * The 'scalate run' sub command.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@command(scope = "scalate", name = "run", description = "Renders a Scalate template file")
class Run extends Action {

  @argument(required = true, name = "template", description = "Template file to render")
  var template: File = _

  @argument(index = 1, multiValued = true, name = "args", description = "Arguments to the template")
  var args: ju.List[String] = new ju.ArrayList[String]

  @option(name = "--workdir", description = "Sets the work directory where scalate generates class files to. Defaults to a temporary directory.")
  var workdir: File = _

  def execute(session: CommandSession): AnyRef = {
    try {
      val engine = new TemplateEngine
      if (workdir != null) {
        engine.workingDirectory = workdir
      }

      val attributes = Map("args" -> args.toList)
      engine.layout(TemplateSource.fromFile(template), attributes)
    } catch {
      case e: Exception =>
        "Error: Could not render: " + template + ". Due to: " + e
    }
  }
}
