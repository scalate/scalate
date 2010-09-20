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
