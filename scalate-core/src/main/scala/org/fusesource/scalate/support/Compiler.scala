package org.fusesource.scalate.support

import java.io.File

trait Compiler {

  def compile(file: File): Unit

  def shutdown(): Unit = {
    // noop
  }

}