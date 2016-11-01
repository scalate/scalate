package org.fusesource.scalate.tool.commands

import java.io.{ File, PrintStream }

import org.apache.felix.gogo.commands
import commands.{ Action, Argument, Command }
import org.apache.felix.service.command.CommandSession
import org.fusesource.scalate.{ TemplateEngine, TemplateSource }

import collection.JavaConverters._
import io.Codec

/** Generates Scala source code from a Scalate template file. */
@Command(scope = "scalate", name = "generate-scala", description = "Converts a Scalate template file to Scala source.")
class GenerateScala extends Action {

  @commands.Option(name = "--boot-class", aliases = Array("-b"), description = "The Scalate framework boot class name.")
  var bootClass: String = _

  @commands.Option(name = "--escape-markup", aliases = Array("-m"), description = "Determines whether sensitive markup characters are escaped for HTML/XML elements.")
  var escapeMarkup: Boolean = true

  @commands.Option(name = "--template-imports", aliases = Array("-i"), multiValued = true, description = "Names of members to be imported by generated Scala sources.")
  var templateImports: java.util.List[String] = _

  @commands.Option(name = "--package-prefix", aliases = Array("-p"), description = "The package prefix of generated Scala sources.")
  var packagePrefix: String = _

  @commands.Option(name = "--working-directory", aliases = Array("-t"), description = "The Scalate framework temporary working directory.")
  var workingDirectory: File = _

  @Argument(index = 0, name = "from", description = "The input file or http URL.", required = true)
  var from: String = _

  @Argument(index = 1, name = "to", description = "The output file. If omitted, output is written to the console")
  var to: File = _

  override def execute(commandSession: CommandSession): AnyRef = {

    def templateSource = from match {
      case f if f.startsWith("http://") || f.startsWith("https://") ⇒ TemplateSource.fromURL(f)
      case f ⇒ TemplateSource.fromFile(f)
    }

    val target = Option(to) map (new PrintStream(_))

    val engine = new TemplateEngine(mode = "production")

    Option(bootClass) foreach (engine.bootClassName = _)

    engine.escapeMarkup = escapeMarkup

    Option(templateImports) map (_.asScala.toList map (s ⇒ s"import $s")) foreach (engine.importStatements = _)

    Option(packagePrefix) foreach (engine.packagePrefix = _)

    Option(workingDirectory) foreach (engine.workingDirectory = _)

    val code = engine.generateScala(templateSource)

    target match {
      case Some(t) ⇒
        t.write(code.source.getBytes(Codec.UTF8.charSet)); t.close()
      case _ ⇒ val o = commandSession.getConsole; o.write(code.source.getBytes(Codec.UTF8.charSet)); o.flush()
    }

    this
  }
}
