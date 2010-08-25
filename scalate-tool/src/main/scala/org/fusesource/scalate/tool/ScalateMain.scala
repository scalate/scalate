package org.fusesource.scalate.tool

import org.apache.karaf.shell.console.Main

object ScalateMain {
  def main(args: Array[String]) = new ScalateMain().run(args)
}

class ScalateMain extends Main {
  setUser("me")
  setApplication("scalate")  
}