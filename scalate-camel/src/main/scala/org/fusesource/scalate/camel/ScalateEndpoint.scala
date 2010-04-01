package org.fusesource.scalate.camel

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.support.DefaultRenderContext
import org.fusesource.scalate.util.{IOUtil}
import org.apache.camel._
import org.apache.camel.component.ResourceBasedEndpoint
import org.apache.camel.util.{ExchangeHelper, ObjectHelper}
import impl.ProcessorEndpoint
import java.io._
import collection.JavaConversions._
import java.util.concurrent.atomic.AtomicInteger
import org.apache.commons.logging.{LogFactory, Log}

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
      val context = new DefaultRenderContext(templateEngine, new PrintWriter(buffer))

      val variableMap = ExchangeHelper.createVariableMap(exchange)
      for ((key, value) <- variableMap) {
        println("setting " + key + " = " + value)
        context.attributes(key) = value
      }
      context.attributes("context") = context
      template.render(context)

      val out = exchange.getOut()
      val response = buffer.toString()

      println("Eval of " + this + " = " + response)
      out.setBody(response)


      /*

      // now lets output the results to the exchange

      Map<String, Object> headers = (Map<String, Object>) variables.get("headers")
      for (String key : headers.keySet()) {
          out.setHeader(key, headers.get(key))
      }
      */
    }
  }

  protected def debug(message: => String):Unit = if (log.isDebugEnabled) {
    val text: String = message
    log.debug(text)
  }
}