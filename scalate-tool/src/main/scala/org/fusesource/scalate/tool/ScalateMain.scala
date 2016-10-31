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
package org.fusesource.scalate.tool

import org.fusesource.scalate.util.IOUtil

import org.apache.felix.gogo.commands.{ Action, Option => option, Argument => argument, Command => command }
import org.apache.karaf.shell.console.Main
import org.apache.karaf.shell.console.jline.Console
import jline.Terminal
import java.io.{ PrintStream, InputStream }
import org.fusesource.jansi.Ansi
import org.apache.felix.service.command.CommandSession
import org.apache.felix.gogo.runtime.CommandProcessorImpl
import org.scalatra.scalate.tool.buildinfo.BuildInfo

object ScalateMain {
  def main(args: Array[String]) = {
    Ansi.ansi()
    new ScalateMain().run(args)
  }

  // Some ANSI helpers...
  def ANSI(value: Any) = "\u001B[" + value + "m"
  val BOLD = ANSI(1)
  val RESET = ANSI(0)
}

@command(scope = "scalate", name = "scalate", description = "Executes a scalate command interpreter")
class ScalateMain extends Main with Action {
  import ScalateMain._

  setUser("me")
  setApplication("scalate")

  var debug = false

  override def getDiscoveryResource = "META-INF/services/org.fusesource.scalate/commands.index"

  override def isMultiScopeMode() = false

  override def createConsole(commandProcessor: CommandProcessorImpl, in: InputStream, out: PrintStream, err: PrintStream, terminal: Terminal) = {
    new Console(commandProcessor, in, out, err, terminal, null) {
      protected override def getPrompt = BOLD + "scalate> " + RESET
      protected override def welcome = {
        session.getConsole().println(IOUtil.loadText(getClass().getResourceAsStream("banner.txt")).replace("${project.version}", BuildInfo.version))
      }
      protected override def setSessionProperties = {}
    }
  }

  @argument(name = "args", description = "scalate sub command arguments", multiValued = true)
  var args = Array[String]()

  def execute(session: CommandSession): AnyRef = {
    run(session, args)
    null
  }

}