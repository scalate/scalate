package org.fusesource.scalate.converter

import java.io.File
import org.fusesource.scalate.util.IOUtil._

/**
 * Converts JSP files into SSP files
 */
class JspConvert extends Runnable {
  var dir: File = new File(".")
  var matchesFile: File => Boolean = isJsp
  var outputFile: File => File = toSsp
  var outputExtension = ".ssp"
  var recursionDepth = -1
  var converter = new JspConverter


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