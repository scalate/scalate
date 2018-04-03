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
package org.fusesource.scalate.spring.view

import org.springframework.web.servlet.view.AbstractTemplateViewResolver
import org.springframework.web.servlet.view.AbstractUrlBasedView
import org.springframework.web.context.ServletConfigAware
import org.fusesource.scalate.servlet.ServletTemplateEngine
import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import java.util.Enumeration
import scala.collection.JavaConverters.asJavaEnumerationConverter

class ScalateViewResolver()
  extends AbstractTemplateViewResolver
  with ServletConfigAware {

  var templateEngine: ServletTemplateEngine = _

  override def setServletConfig(config: ServletConfig): Unit = {
    val ste = new ServletTemplateEngine(config)
    ServletTemplateEngine(config.getServletContext()) = ste
    templateEngine = ste
  }

  override def initServletContext(servletContext: ServletContext): Unit = {
    super.initServletContext(servletContext)

    setServletConfig(new ServletConfig() {
      def getServletName(): String = "unknown"
      def getServletContext(): ServletContext = servletContext
      def getInitParameterNames(): Enumeration[String] = List[String]().iterator.asJavaEnumeration
      def getInitParameter(s: String) = null
    });
  }

  setViewClass(requiredViewClass())

  override def requiredViewClass(): java.lang.Class[_] = classOf[org.fusesource.scalate.spring.view.ScalateView]

  override def buildView(viewName: String): AbstractUrlBasedView = {
    var view: AbstractScalateView = null

    if (viewName == "view") {
      view = new ScalateView
    } else if (viewName.startsWith("layout:")) {
      val urlView = new ScalateUrlView with LayoutScalateRenderStrategy
      urlView.setUrl(getPrefix() + viewName.substring("layout:".length()) + getSuffix())
      view = urlView
    } else {
      val urlView = new ScalateUrlView with DefaultScalateRenderStrategy
      urlView.setUrl(getPrefix() + viewName + getSuffix())
      view = urlView
    }

    view.templateEngine = templateEngine

    val contentType = getContentType
    if (contentType != null) {
      view.setContentType(contentType)
    }

    view.asInstanceOf[AbstractUrlBasedView]
  }

}
