package org.fusesource.scalate.camel

import org.apache.camel.component.ResourceBasedEndpoint
import org.apache.camel._
import java.io._
import org.apache.camel.util.{ExchangeHelper, ObjectHelper}
import org.fusesource.scalate.util.Logging
import org.fusesource.scalate.TemplateEngine

/**
 * @version $Revision : 1.1 $
 */

class ScalateEndpoint(uri: String, component: ScalateComponent, templateUri: String,
                      templateEngine: TemplateEngine = new TemplateEngine(), encoding: String = null)
        extends ResourceBasedEndpoint(uri, component, templateUri, null) {
  val RESOURCE_URI = "CamelScalateResourceUri"
  val TEMPLATE = "CamelScalateTemplate"

  override def isSingleton = true

  override def getExchangePattern = ExchangePattern.InOut

  override def createEndpointUri = "scalate:" + templateUri


  def findOrCreateEndpoint(uri: String, newResourceUri: String): ScalateEndpoint = {
    val newUri = uri.replace(getResourceUri(), newResourceUri)
    debug("Getting endpoint with URI: " + newUri)
    getCamelContext.getEndpoint(newUri, classOf[ScalateEndpoint])
  }


  override def onExchange(exchange: Exchange) = {
    val path = getResourceUri()
    ObjectHelper.notNull(path, "resourceUri")

    val newResourceUri = exchange.getIn().getHeader(RESOURCE_URI, classOf[String])
    if (newResourceUri != null) {
      exchange.getIn().removeHeader(RESOURCE_URI)

      debug(RESOURCE_URI + " set to " + newResourceUri + " creating new endpoint to handle exchange")

      val newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri)
      newEndpoint.onExchange(exchange)
    }
    else {

      val content = exchange.getIn().getHeader(TEMPLATE, classOf[String])
      val reader = if (content != null) {
        debug("Velocity content read from header " + TEMPLATE + " for endpoint " + getEndpointUri())

        // remove the header to avoid it being propagated in the routing
        exchange.getIn().removeHeader(TEMPLATE)

        // use content from header
        new StringReader(content)
      } else {
        // use resource from endpoint configuration
        val resource = getResource()
        ObjectHelper.notNull(resource, "resource")
        debug("Velocity content read from resource " + resource + " with resourceUri: " + path + " for endpoint " + getEndpointUri())

        if (encoding != null) {
          new InputStreamReader(getResourceAsInputStream(), encoding)
        } else {new InputStreamReader(getResourceAsInputStream())}
      }

      // getResourceAsInputStream also considers the content cache
      val buffer = new StringWriter()
      val logTag = getClass().getName()
      val variableMap = ExchangeHelper.createVariableMap(exchange)


      templateEngine.load(path)

      /*

      // now lets output the results to the exchange
      Message out = exchange.getOut()
      out.setBody(buffer.toString())

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