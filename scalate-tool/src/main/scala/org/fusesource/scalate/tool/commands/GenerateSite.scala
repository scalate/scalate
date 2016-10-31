package org.fusesource.scalate.tool.commands

import java.io.File

import org.apache.felix.gogo.commands
import commands.{ Action, Argument, Command }
import org.apache.felix.service.command.CommandSession
import org.fusesource.scalate.support.SiteGenerator

/** Generates a static website from a Scalate template file directory. */
@Command(scope = "scalate", name = "generate-site", description = "Generates a static website from Scalate templates.")
class GenerateSite extends Action {

  @commands.Option(name = "--boot-class", aliases = Array("-b"), description = "The Scalate framework boot class name.")
  var bootClass: String = _

  @commands.Option(name = "--working-directory", aliases = Array("-t"), description = "The Scalate framework temporary working directory.")
  var workingDirectory: File = _

  @Argument(index = 0, name = "from", description = "The input file directory.", required = true)
  var from: File = _

  @Argument(index = 1, name = "to", description = "The output directory.", required = true)
  var to: File = _

  override def execute(commandSession: CommandSession): AnyRef = {
    def infoF = (m: String) ⇒ { val c = commandSession.getConsole; c.print(m); c.flush() }

    val generator = new SiteGenerator

    to.mkdirs()

    Option(bootClass) foreach (generator.bootClassName = _)

    Option(workingDirectory) foreach (generator.workingDirectory = _)

    generator.info = infoF

    generator.webappDirectory = from

    generator.targetDirectory = to

    generator.execute()

    this
  }
}
