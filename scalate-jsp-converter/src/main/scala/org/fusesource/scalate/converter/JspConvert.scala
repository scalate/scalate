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
package org.fusesource.scalate.converter

import java.io.File
import java.{ lang => jl }
import org.fusesource.scalate.util.IOUtil._

import org.apache.felix.service.command.CommandSession
import org.apache.karaf.shell.api.action.{ Option => option, Argument => argument, Command => command }

/**
 * Converts JSP files into SSP files
 */
@command(scope = "scalate", name = "jsp2ssp", description = "Converts JSP files to SSP files")
class JspConvert extends Runnable {
  @argument(index = 0, name = "dir", description = "Root of the directory containing the JSP files.")
  var dir: File = new File(".")

  @option(name = "--extension", description = "Extension for output files")
  var outputExtension = ".ssp"
  @option(name = "--recursion", description = "The number of directory levels to recusively scan file input files.")
  var recursionDepth = -1
  @option(name = "--jaxrs", description = "If in JAXRS mode we will add the 'it' attribute if a template looks like its a resource template.")
  var jaxrs = false
  @option(name = "--conciseTemplates", description = "If using JAXRS templates should we put templates in the same directory as the package (rather than a directory per controller).")
  var conciseJaxrsTemplates = true

  var converter = new JspConverter
  var matchesFile: File => Boolean = isJsp
  var outputFile: File => File = toSsp

  /**
   * Runs the command given the command line arguments
   */
  def execute(session: CommandSession): jl.Integer = run(dir)

  /**
   * Runs the command given the command line arguments
   */
  def run(): Unit = run(dir)

  /**
   * Recurses down the di
   */
  def run(file: File, level: Int = 0): Int = {
    var count = 0
    if (file.exists) {
      if (file.isDirectory) {
        if (recursionDepth < 0 || level < recursionDepth) {
          val nextLevel = level + 1
          for (f <- file.listFiles) {
            count += run(f, nextLevel)
          }
        }
      } else {
        if (matchesFile(file)) {
          convert(file)
          count += 1
        }
      }
    }
    count
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
      } else if (name(0).isUpper) {
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
