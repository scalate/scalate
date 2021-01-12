package org.fusesource.scalate.util

object Threads {
  def thread(name: String)(func: => Unit): Unit = {
    new Thread(name) {
      override def run = {
        func
      }
    }.start()
  }
}
