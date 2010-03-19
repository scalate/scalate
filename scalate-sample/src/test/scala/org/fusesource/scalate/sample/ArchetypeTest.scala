package org.fusesource.scalate.sample


import org.fusesource.scalate._
import org.fusesource.scalate.util._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import java.io.File
import util.Logging

@RunWith(classOf[JUnitRunner])
class ArchetypeTest extends FunSuite with Logging {
  val engine = new TemplateEngine
  engine.workingDirectory = new File("target/test-data/ArchetypeTest")

  test("use tableView archetype") {
    val output = engine.layout("src/main/webapp/WEB-INF/archetypes/index/tableView.ssp", Map("resourceType" -> classOf[Person])).trim

    println("Generated SSP:")
    println(output)
  }
}