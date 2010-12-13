package org.fusesource.scalate
package wikitext

object Main {

  def main(args: Array[String]): Unit = {
    for (a <- args) {
      parseFile(a)
    }
  }

  def parseFile(name: String): Unit = {
    val engine = new TemplateEngine
    val output = engine.layout(TemplateSource.fromFile(name))
    println(output)
  }
}