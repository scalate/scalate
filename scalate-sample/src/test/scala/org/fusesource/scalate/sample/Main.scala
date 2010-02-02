package org.fusesource.scalate.sample

/**
 * @version $Revision: 1.1 $
 */

object Main {

  def main(args: Array[String]): Unit = {
    var server = new JettyServer
    if (args.length > 0) {
      var text = args(0)
      server.port = Integer.parseInt(text)
    }
    server.start
  }

}