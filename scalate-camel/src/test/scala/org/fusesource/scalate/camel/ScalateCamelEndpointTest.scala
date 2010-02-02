package org.fusesource.scalate.camel


import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import org.fusesource.scalate._
import org.apache.camel.builder.RouteBuilder
import org.apache.camel._
import org.apache.camel.impl.DefaultCamelContext

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class CamelScalateEndpointTest extends FunSuite {
  val uriPrefix = "scalate:org/fusesource/scalate/camel/"

  scenario(uriPrefix + "constant.ssp", "James", "<hello>James</hello>")
  //scenario(uriPrefix + "example.ssp", "James", "<hello>James</hello>")

  /**
   * Processes the route builder using the given callback function which processes a producer template on the constructed
   * camel context
   */
  def withRouteBuilder(routeBuilder: RouteBuilder)(useTemplate: (ProducerTemplate) => Unit): Unit = {
    val context = new DefaultCamelContext()
    context.addRoutes(routeBuilder)
    try {
      context.start()

      val template = context.createProducerTemplate()
      useTemplate(template)
    }
    finally {
      context.stop()
    }
  }


  def scenario(uri: String, body: Any, expectedResult: String): Unit = {
    test("routing to endpoint " + uri) {
      val startUri = "direct:start"
      val headerValue = "someHeaderValue"

      val routeBuilder = new RouteBuilder() {
        def configure(): Unit = {
          from(startUri).to(uri)
        }
      }
      withRouteBuilder(routeBuilder) {
        template =>
          val response = template.request(startUri, new Processor() {
            def process(exchange: Exchange): Unit = {
              val in = exchange.getIn
              in.setBody(body)
              in.setHeader("cheese", headerValue)

              println("sending body: " + exchange.getIn.getBody)
            }
          })

          val out = response.getOut
          assume(out != null, "out was null when sending to uri: " + uri + " body: " + body)

          expect(expectedResult) {
            out.getBody(classOf[String])
          }
      }
    }
  }


}
