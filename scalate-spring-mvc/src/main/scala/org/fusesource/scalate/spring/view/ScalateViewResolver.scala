package org.fusesource.scalate.spring.view

import java.util.Locale
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.AbstractCachingViewResolver

class ScalateViewResolver extends AbstractCachingViewResolver {

  override def loadView(viewName:String, locale:Locale) : View = {

    var view:View = null

    if (viewName == "view") {
      view = new ScalateView
    } else if (viewName.startsWith("render:")) {
      val urlView = new ScalateUrlView with DefaultScalateRenderStrategy
      urlView.setUrl(viewName.replaceFirst("render:",""))
      view = urlView
    } else {
      val urlView = new ScalateUrlView with LayoutScalateRenderStrategy
      urlView.setUrl(viewName)
      view = urlView
    }

    // needed for spring magic on the view.  views are cached, so this will only be a one-time call
    if (view != null)
      getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);

    return view
  }

}