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
package org.fusesource.scalate.converter

import java.io.File
import org.fusesource.scalate.util.IOUtil._
import org.fusesource.scalate.tool.Command
import com.beust.jcommander.Parameter

object JspConvertCommand extends Command {
  def name = "jsp2ssp"

  def summary = "Converts JSP files to SSP files"

  def usage() = {
    val commander = JCommanderHelper.create(new JspConvert, new HelpSettings)
    // the next release of JCommander will allows setting the main program name
    commander.usage
  }

  def process(args: List[String]) = {
    main(args.toArray)
    0
  }

  def main(args: Array[String]) = {
    val converter = new JspConvert
    val help = new HelpSettings
    JCommanderHelper.create(converter, help).parse(args: _*)
    if (help.help) {
      usage
    } else {
      converter.run
    }
  }

}

class HelpSettings {
  @Parameter(names = Array("--help"), description = "Displays this usage screen.")
  var help = false
}

/**
 * Converts JSP files into SSP files
 */
class JspConvert extends Runnable {
  @Parameter(names = Array("--directory"), description = "Root of the directory containing the JSP files.")
  var dir: File = new File(".")
  @Parameter(names = Array("--extension"), description = "Extension for output files")
  var outputExtension = ".ssp"
  @Parameter(names = Array("--recursion"), description = "The number of directroy levels to recusively scan file input files.")
  var recursionDepth = -1
  @Parameter(names = Array("--jaxrs"), description = "If in JAXRS mode we will add the 'it' attribute if a template looks like its a resource template.")
  var jaxrs = false
  @Parameter(names = Array("--conciseTemplates"), description = "If using JAXRS templates should we put templates in teh same directory as the package (rather than a directory per controller).")
  var conciseJaxrsTemplates = true

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
    outFile.text = postProcessSsp(file, ssp)
  }

  /**
   * Lets try and detect the 'it' attribute for JAXRS named templates
   */
  protected def postProcessSsp(file: File, ssp: String): String = {
    var className: Option[String] = None

    if (jaxrs) {
      // lets try to detect the class name
      val parentName = file.getParentFile.getName
      val name = file.getName

      if (parentName(0).isUpper) {
        className = Some(parentName)
      }
      else if (name(0).isUpper) {
        // lets assume the first name before dot is the name
        className = Some(name.split('.').head)
      }
    }
    className match {
      case Some(n) => "<%@ import val it: " + n + " %>\n" + ssp
      case _ => ssp
    }
  }

  protected def isJsp(file: File): Boolean = file.getName.toLowerCase.endsWith(".jsp")

  protected def toSsp(file: File): File = {
    val parentDir = file.getParentFile
    val parentName = parentDir.getName
    val grandParentDir = parentDir.getParentFile

    val name = file.getName
    val nameWithoutExtension = if (isJsp(file)) name.dropRight(4) else name

    def isInControllerDir = parentName(0).isUpper && name(0).isLower

    if (jaxrs && conciseJaxrsTemplates && grandParentDir != null && isInControllerDir) {
      new File(grandParentDir, parentName + "." + nameWithoutExtension + outputExtension)
    } else {
      new File(parentDir, nameWithoutExtension + outputExtension)
    }
  }
}