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

package org.fusesource.scalate.spring.view

import _root_.javax.servlet.ServletConfig
import _root_.javax.servlet.http.HttpServletRequest
import _root_.javax.servlet.http.HttpServletResponse
import _root_.org.fusesource.scalate.servlet.ServletRenderContext
import _root_.org.fusesource.scalate.servlet.ServletTemplateEngine
import _root_.org.springframework.web.context.ServletConfigAware
import _root_.scala.collection.JavaConversions._
import _root_.org.fusesource.scalate.TemplateException
import _root_.org.springframework.web.servlet.view.{AbstractView, AbstractTemplateView}

trait ScalateRenderStrategy {
  def render(context:ServletRenderContext, model: Map[String,Any]);
}

trait LayoutScalateRenderStrategy extends AbstractTemplateView with ScalateRenderStrategy {
  def templateEngine:ServletTemplateEngine
  def render(context:ServletRenderContext, model: Map[String,Any]) {
    templateEngine.layout(getUrl, context)
  }
}

trait DefaultScalateRenderStrategy extends AbstractTemplateView with ScalateRenderStrategy {
  override def render(context:ServletRenderContext, model: Map[String,Any]) {
    context.render(getUrl, model)
  }
}

trait ViewScalateRenderStrategy extends ScalateRenderStrategy {
  override def render(context:ServletRenderContext, model: Map[String,Any]) {
    val it = model.get("it")
    if (it.isEmpty)
      throw new TemplateException("No 'it' model object specified.  Cannot render request")
    context.view(it.get.asInstanceOf[AnyRef])
  }
}


trait AbstractScalateView extends AbstractView with ServletConfigAware {
  var templateEngine:ServletTemplateEngine = _;

  def setServletConfig(config:ServletConfig) {
    templateEngine = new ServletTemplateEngine(config);
  }
}

class ScalateUrlView extends AbstractTemplateView with AbstractScalateView
        with ServletConfigAware with LayoutScalateRenderStrategy  {

  override def renderMergedTemplateModel(model: java.util.Map[String, Object],
                                         request: HttpServletRequest,
                                         response: HttpServletResponse) : Unit = {

    val context = new ServletRenderContext(templateEngine, request, response, getServletContext)
    render(context, model.asInstanceOf[java.util.Map[String,Any]].toMap)
  }

}

class ScalateView extends AbstractScalateView with ViewScalateRenderStrategy {

  override def renderMergedOutputModel(model: java.util.Map[String, Object],
                                         request: HttpServletRequest,
                                         response: HttpServletResponse) : Unit = {

    val context = new ServletRenderContext(templateEngine, request, response, getServletContext)
    render(context, model.asInstanceOf[java.util.Map[String,Any]].toMap)
  }

}
