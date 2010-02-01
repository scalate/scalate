package org.fusesource.scalate.camel

import org.apache.camel.Exchange
import org.apache.camel.Message
import org.apache.camel.Processor
import org.apache.camel.test.junit4.CamelTestSupport
import org.apache.camel.test.junit4.TestSupport._
import org.junit.Assert._
import org.junit.Test
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.RouteBuilder._
import org.apache.camel.builder.BuilderSupport._


/**
 * @version $Revision : 1.1 $
 */

class ScalateCamelTest extends CamelTestSupport {
  @Test
  def testMessageIsTransformedByScalate(): Unit = {
    assertRespondsWith("James", "<hello>James</hello>")
  }

  override def createRouteBuilder() = new RouteBuilder() {
    def configure(): Unit = {
      from("direct:a").
              to("scalate:org/fusesource/scalate/camel/example.ssp")
    }
  }

  def assertRespondsWith(value: String, expectedBody: String): Unit = {
    val response = try {
      template.request("direct:a", new Processor() {
        def process(exchange: Exchange): Unit = {
          val in = exchange.getIn
          in.setBody("answer")
          in.setHeader("cheese", value)
        }
      })
    }
    catch {
      case e: Exception => println("Failed: " + e)
      log.error("Failed: " + e, e)
      fail(e.getMessage)
      null
    }
    assertOutMessageBodyEquals(response, expectedBody)
  }


}