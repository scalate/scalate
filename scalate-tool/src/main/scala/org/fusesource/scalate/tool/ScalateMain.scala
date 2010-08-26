package org.fusesource.scalate.tool

import org.osgi.service.command.CommandSession
import org.apache.felix.gogo.commands.{Action, Option => option, Argument => argument, Command => command}
import jline.Terminal
import java.io.{PrintStream, InputStream}
import org.apache.felix.gogo.runtime.shell.CommandShellImpl
import org.apache.karaf.shell.console.jline.Console
import org.fusesource.scalate.util.IOUtil
import org.apache.karaf.shell.console.{Completer, Main}

object ScalateMain {
  def main(args: Array[String]) = new ScalateMain().run(args)

  // Some ANSI helpers...
  def ANSI(value:Any) =  "\u001B["+value+"m"
  val BOLD =  ANSI(1)
  val RESET = ANSI(0)
}

@command(scope = "scalate", name = "scalate", description = "Executes a scalate command interpreter")
class ScalateMain extends Main with Action {
  import ScalateMain._

  setUser("me")
  setApplication("scalate")

  var debug = false

  override def getDiscoveryResource = "META-INF/services/org.fusesource.scalate/commands.index"

    protected override def createConsole(commandProcessor: CommandShellImpl, in: InputStream, out: PrintStream, err: PrintStream, terminal: Terminal, completer: Completer) = {
    new Console(commandProcessor, in, out, err, terminal, completer, null) {
      protected override def getPrompt = BOLD+"scalate> "+RESET
      protected override def isPrintStackTraces = debug
      protected override def welcome = {
         session.getConsole().println(IOUtil.loadText(getClass().getResourceAsStream("banner.txt")))
      }
      protected override def setSessionProperties = {}
    }
  }

  @argument(name = "args", description = "scalate sub command arguments", multiValued=true)
  var args = Array[String]()

  def execute(session: CommandSession): AnyRef = {
    run(session, args)
    null
  }


}