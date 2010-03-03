package org.fusesource.scalate.camel

import org.apache.camel.impl.DefaultComponent
import java.util.Map
import java.lang.String
import org.apache.camel.Endpoint
import org.fusesource.scalate.{FileResourceLoader, TemplateEngine}
import java.io.File
import org.springframework.core.io.DefaultResourceLoader

/**
 * @version $Revision : 1.1 $
 */

class ScalateComponent(var defaultTemplateExtension: String = "ssp") extends DefaultComponent {
  
  var templateEngine: TemplateEngine = new TemplateEngine()

  def createEndpoint(uri: String, remaining: String, parameters: Map[String, Object]): Endpoint = {
    new ScalateEndpoint(this, uri, remaining, defaultTemplateExtension)
  }
}