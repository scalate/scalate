package org.fusesource.scalate.sample


import _root_.org.fusesource.scalate._
import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSuite
import _root_.org.scalatest.junit.JUnitRunner
import _root_.java.io.File
import util.Logging

case class Person(first: String, last: String) {
}

@RunWith(classOf[JUnitRunner])
class ArchetypeTest extends FunSuite with Logging {
  val engine = new TemplateEngine
  engine.workingDirectory = new File("target/test-data/ArchetypeTest")

  test("use tableView archetype") {
    val output = engine.layout("src/main/webapp/WEB-INF/scalate/archetypes/views/index/tableView.ssp", Map("resourceType" -> classOf[Person])).trim

    println("Generated SSP:")
    println(output)
  }
}