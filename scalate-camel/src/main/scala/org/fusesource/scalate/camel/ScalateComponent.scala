package org.fusesource.scalate.camel

import org.apache.camel.impl.DefaultComponent
import java.util.Map
import java.lang.String
import org.fusesource.scalate.TemplateEngine
import org.apache.camel.Endpoint

/**
 * @version $Revision : 1.1 $
 */

class ScalateComponent(templateEngine: TemplateEngine = new TemplateEngine()) extends DefaultComponent {
  def createEndpoint(uri: String, remaining: String, parameters: Map[String, Object]): Endpoint = {
    new ScalateEndpoint(uri, this, remaining, templateEngine)
  }

}