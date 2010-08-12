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
import org.fusesource.scalate.tool.CommandFactory
import com.beust.jcommander.shell.Shell
import com.beust.jcommander.{JCommander, Parameter, Command}

object Help extends CommandFactory {

  def name = "help"
  def create = create(new Help)
  
}

/**
 * The 'scalate help' sub command.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Command(description = "Displays this help or the help on a command")
class Help extends Runnable {

  @Parameter(description = "The sub command to display help on")
  var command: java.util.ArrayList[String] = _

  def run() = {
    val session = Shell.getCurrentSession
    if( command == null || command.size < 1) {
        session.getJCommander.usage
    } else {
      val name = command.get(0)
      session.getShell.createSubCommand(name) match {
        case jc:JCommander =>
          jc.usage
        case null =>
          System.out.println("invalid command: "+name)
          session.getJCommander.usage
      }
    }
  }

}
