/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.scalate.camel


import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.FunSuite
import _root_.java.io.File
import _root_.org.fusesource.scalate._
import _root_.org.apache.camel.builder.RouteBuilder
import _root_.org.apache.camel._
import _root_.org.apache.camel.impl.DefaultCamelContext

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitRunner])
class CamelScalateEndpointTest extends FunSuite {
  val uriPrefix = "scalate:org/fusesource/scalate/camel/"

  scenario(uriPrefix + "constant.ssp", "James", "<hello>James</hello>")
  scenario(uriPrefix + "example.ssp", "James", "<hello>James</hello>")

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
            out.getBody(classOf[String]).trim()
          }
      }
    }
  }


}
