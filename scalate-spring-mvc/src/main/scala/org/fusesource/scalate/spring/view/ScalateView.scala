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

import _root_.java.util.Locale
import _root_.javax.servlet.http.HttpServletRequest
import _root_.javax.servlet.http.HttpServletResponse
import _root_.org.fusesource.scalate.RenderContext
import _root_.org.fusesource.scalate.servlet.ServletRenderContext
import _root_.org.fusesource.scalate.servlet.ServletTemplateEngine
import _root_.scala.jdk.CollectionConverters._
import _root_.org.fusesource.scalate.TemplateException
import _root_.org.springframework.web.servlet.view.{ AbstractView, AbstractTemplateView }
import _root_.org.slf4j.LoggerFactory
import org.fusesource.scalate.util.ResourceNotFoundException

trait ScalateRenderStrategy {

  protected val log = LoggerFactory.getLogger(getClass)

  def render(context: ServletRenderContext, model: Map[String, Any]): Unit

}

trait LayoutScalateRenderStrategy
  extends AbstractTemplateView
  with ScalateRenderStrategy {

  def templateEngine: ServletTemplateEngine

  def render(context: ServletRenderContext, model: Map[String, Any]): Unit = {
    log.debug("Rendering view with name '" + getUrl + "' with model " + model)
    for ((key, value) <- model) {
      context.attributes(key) = value
    }
    templateEngine.layout(getUrl, context)
  }
}

trait DefaultScalateRenderStrategy
  extends AbstractTemplateView
  with ScalateRenderStrategy {

  override def render(context: ServletRenderContext, model: Map[String, Any]): Unit = {
    log.debug("Rendering view with name '" + getUrl + "' with model " + model)
    context.render(getUrl, model)
  }
}

trait ViewScalateRenderStrategy
  extends ScalateRenderStrategy {

  override def render(context: ServletRenderContext, model: Map[String, Any]): Unit = {
    log.debug("Rendering with model " + model)
    val it = model.get("it")
    if (it.isEmpty)
      throw new TemplateException("No 'it' model object specified.  Cannot render request")
    context.view(it.get.asInstanceOf[AnyRef])
  }
}

trait AbstractScalateView extends AbstractView {

  var templateEngine: ServletTemplateEngine = _

  def checkResource(locale: Locale): Boolean

}

class ScalateUrlView
  extends AbstractTemplateView
  with AbstractScalateView
  with LayoutScalateRenderStrategy {

  override def renderMergedTemplateModel(
    model: java.util.Map[String, Object],
    request: HttpServletRequest,
    response: HttpServletResponse): Unit = {

    val context = new ServletRenderContext(templateEngine, request, response, getServletContext)
    RenderContext.using(context) {
      render(context, model.asInstanceOf[java.util.Map[String, Any]].asScala.toMap)
    }
  }

  override def checkResource(locale: Locale): Boolean = try {
    log.debug("Checking for resource " + getUrl)
    templateEngine.load(getUrl)
    true
  } catch {
    case e: ResourceNotFoundException => {
      log.info("Could not find resource " + getUrl)
      false
    }
  }

}

class ScalateView
  extends AbstractScalateView
  with ViewScalateRenderStrategy {

  override def checkResource(locale: Locale) = true

  override def renderMergedOutputModel(
    model: java.util.Map[String, Object],
    request: HttpServletRequest,
    response: HttpServletResponse): Unit = {

    val context = new ServletRenderContext(templateEngine, request, response, getServletContext)
    RenderContext.using(context) {
      render(context, model.asInstanceOf[java.util.Map[String, Any]].asScala.toMap)
    }
  }

}
