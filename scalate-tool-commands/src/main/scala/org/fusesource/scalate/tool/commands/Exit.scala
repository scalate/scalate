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
import com.beust.jcommander.Command
import com.beust.jcommander.shell.Shell
import com.beust.jcommander.shell.Shell.CloseShellException

object Exit extends CommandFactory {

  def name = "exit"

  def create = {
    if(Shell.getCurrentSession !=null ) {
      create(new Exit)
    } else {
      null
    }
  }
}

/**
 * The 'scalate exit' sub command.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@Command(description = "Exits the shell")
class Exit extends Runnable {

  def run = throw new CloseShellException();

}
