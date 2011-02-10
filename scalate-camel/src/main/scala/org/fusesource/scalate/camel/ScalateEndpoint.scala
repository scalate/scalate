/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate
package camel

import java.io._
import java.{util => ju}
import org.apache.camel._
import org.apache.camel.util.{ExchangeHelper, ObjectHelper}

import org.apache.commons.logging.LogFactory

import impl.ProcessorEndpoint
import collection.JavaConversions._

/**
 * @version $Revision : 1.1 $
 */

class ScalateEndpoint(component: ScalateComponent, uri: String,  templateUri: String, defaultTemplateExtension: String = "ssp")
        extends ProcessorEndpoint(uri, component) {

  val log = LogFactory.getLog(getClass); 

  val RESOURCE_URI = "CamelScalateResourceUri"
  val TEMPLATE = "CamelScalateTemplate"

  override def isSingleton = true

  override def getExchangePattern = ExchangePattern.InOut

  override def createEndpointUri = "scalate:" + templateUri

  def findOrCreateEndpoint(uri: String, newResourceUri: String): ScalateEndpoint = {
    val newUri = "scalate:" + component.templateEngine.resourceLoader.resolve(templateUri, newResourceUri)
    debug("Getting endpoint with URI: " + newUri)
    getCamelContext.getEndpoint(newUri, classOf[ScalateEndpoint])
  }


  override def onExchange(exchange: Exchange):Unit = {
    ObjectHelper.notNull(templateUri, "resourceUri")
    val templateEngine = component.templateEngine;
    val newResourceUri = exchange.getIn().getHeader(RESOURCE_URI, classOf[String])
    if (newResourceUri != null) {
      exchange.getIn().removeHeader(RESOURCE_URI)

      debug(RESOURCE_URI + " set to " + newResourceUri + " creating new endpoint to handle exchange")

      val newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri)
      newEndpoint.onExchange(exchange)
    }
    else {

      val content = exchange.getIn().getHeader(TEMPLATE, classOf[String])
      val template = if (content != null) {
        // use content from header
        debug("Scalate content read from header " + TEMPLATE + " for endpoint " + getEndpointUri())

        // remove the header to avoid it being propagated in the routing
        exchange.getIn().removeHeader(TEMPLATE)

        templateEngine.compileText(defaultTemplateExtension, content)
      } else {
        templateEngine.load(templateUri)
      }

      //val logTag = getClass().getName()
      val buffer = new StringWriter()
      val context = new DefaultRenderContext(uri, templateEngine, new PrintWriter(buffer))

      val variableMap = ExchangeHelper.createVariableMap(exchange)
      for ((key, value) <- variableMap) {
        debug("setting " + key + " = " + value)
        context.attributes(key) = value
      }
      context.attributes("context") = context
      template.render(context)

      val out = if (exchange.getPattern.isOutCapable) exchange.getOut() else exchange.getIn
      val response = buffer.toString()

      debug("Eval of " + this + " = " + response)
      out.setBody(response)

      // now lets output the headers to the exchange
      variableMap.get("headers") match {
        case map: ju.Map[String,AnyRef] =>
          for ((key, value) <- map) {
            out.setHeader(key, value)
          }
        case _ =>
      }
    }
  }

  protected def debug(message: => String):Unit = if (log.isDebugEnabled) {
    val text: String = message
    log.debug(text)
  }
}