package org.fusesource.scalate.converter

import org.fusesource.scalate.tool.Scalate

/**
 * Creates a dummy shell for testing out the scalate tool commmand line argument parsing
 *
 */
object DummyShell {
  def main(args: Array[String]): Unit = {
    println("Testing the Scalate tool")
    println("Type in the commands you want to execute within the shell or 'exit' to finish")

    var ok = true
    while (ok) {
      val line = Console.readLine.trim
      if (line == null || line.toLowerCase == "exit") {
        ok = false
      }
      else {
        val lineArgs = if (line == "") Array[String]() else line.split("\\s+")
        Scalate.main(lineArgs)
      }
    }
  }
}