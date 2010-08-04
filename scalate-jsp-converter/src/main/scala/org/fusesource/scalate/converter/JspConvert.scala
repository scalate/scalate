package org.fusesource.scalate.converter

import java.io.File
import org.fusesource.scalate.util.IOUtil._
import org.fusesource.scalate.tool.Command
import com.beust.jcommander.{JCommander, Parameter}

object JspConvertCommand extends Command {

  def name = "jsp2ssp"

  def summary = "Converts JSP files to SSP files"

  def usage() = {
    val commander = JCommanderHelper.create(new JspConvert, new HelpSettings)
    // the next release of JCommander will allows setting the main program name
    commander.usage
  }

  def process(args:List[String]) = {
    main( args.toArray )
    0
  }

  def main(args:Array[String]) = {
    val converter = new JspConvert
    val help = new HelpSettings
    JCommanderHelper.create(converter, help).parse(args: _*)
    if( help.help ) {
      usage
    } else {
      converter.run
    }
  }

}

class HelpSettings {
  @Parameter(names=Array("--help"), description="Displays this usage screen.")
  var help = false
}

/**
 * Converts JSP files into SSP files
 */
class JspConvert extends Runnable {

  @Parameter(names=Array("--directory"), description="Root of the directory containing the JSP files.")
  var dir: File = new File(".")
  @Parameter(names=Array("--extension"), description="Extension for output files")
  var outputExtension = ".ssp"
  @Parameter(names=Array("--recursion"), description="The number of directroy levels to recusively scan file input files.")
  var recursionDepth = -1

  var converter = new JspConverter
  var matchesFile: File => Boolean = isJsp
  var outputFile: File => File = toSsp

  /**
   * Recurses down the
   */
  def run: Unit = {
    run(dir)
  }

  def run(file: File, level: Int = 0): Unit = {
    if (file.exists) {
      if (file.isDirectory) {
        if (recursionDepth < 0 || level < recursionDepth) {
          val nextLevel = level + 1
          for (f <- file.listFiles) {
            run(f, nextLevel)
          }
        }
      }
      else {
        if (matchesFile(file)) {
          convert(file)
        }
      }
    }
  }

  def convert(file: File): Unit = {
    println("About to convert JSP file: " + file)
    val ssp = converter.convert(file.text)
    val outFile = outputFile(file)
    outFile.text = ssp
  }

  protected def isJsp(file: File): Boolean = file.getName.toLowerCase.endsWith(".jsp")

  protected def toSsp(file: File): File = {
    val name = file.getName
    val newName = if (isJsp(file)) {
      name.dropRight(4) + outputExtension
    }
    else {
      name + outputExtension
    }
    new File(file.getParentFile, newName)
  }
}