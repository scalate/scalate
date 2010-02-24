package org.fusesource.scalate.camel

import org.apache.camel.component.ResourceBasedEndpoint
import org.apache.camel._
import java.io._
import org.apache.camel.util.{ExchangeHelper, ObjectHelper}
import org.fusesource.scalate.util.{IOUtil}
import org.fusesource.scalate.{DefaultRenderContext, TemplateEngine}
import collection.JavaConversions._

/**
 * @version $Revision : 1.1 $
 */

class ScalateEndpoint(uri: String, component: ScalateComponent, templateUri: String,
                      templateEngine: TemplateEngine = new TemplateEngine(), defaultTemplateExtension: String = "ssp")
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


  override def onExchange(exchange: Exchange):Unit = {
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
      val template = if (content != null) {
        // use content from header
        debug("Scalate content read from header " + TEMPLATE + " for endpoint " + getEndpointUri())

        // remove the header to avoid it being propagated in the routing
        exchange.getIn().removeHeader(TEMPLATE)

        // lets create a new temporary file for now as the API does not yet support a reader...
        val tempFile = File.createTempFile("scalate_", "." + defaultTemplateExtension)
        IOUtil.copy(new StringReader(content), new FileWriter(tempFile))

        val template = templateEngine.compile(uri)
        tempFile.delete
        template

      } else {
        // use resource from endpoint configuration
        val resource = getResource()            
        ObjectHelper.notNull(resource, "resource")
        debug("Scalate content read from resource " + resource + " with resourceUri: " + path + " for endpoint " + getEndpointUri())

        val uri = resource.getFile.getCanonicalPath
        templateEngine.load(uri)
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