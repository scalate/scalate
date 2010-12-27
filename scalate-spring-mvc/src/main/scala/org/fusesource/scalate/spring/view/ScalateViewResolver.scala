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

import java.util.Locale

import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.AbstractCachingViewResolver

import scala.reflect.BeanProperty

trait Ordered extends org.springframework.core.Ordered

class ScalateViewResolver() extends AbstractCachingViewResolver with Ordered {

  @BeanProperty var order: Int = 1
  @BeanProperty var prefix: String = ""
  @BeanProperty var suffix: String = ""

  override def loadView(viewName: String, locale: Locale): View = {

    var view: AbstractScalateView = null

    if (viewName == "view") {
      view = new ScalateView
    } else if (viewName.startsWith("layout:")) {
      val urlView = new ScalateUrlView with LayoutScalateRenderStrategy
      urlView.setUrl(prefix + viewName.substring("layout:".length()) + suffix)
      view = urlView
    } else {
      val urlView = new ScalateUrlView with DefaultScalateRenderStrategy
      urlView.setUrl(prefix + viewName + suffix)
      view = urlView
    }

    // needed for spring magic on the view.  views are cached, so this will only be a one-time call
    if (view != null)
      getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
      if (!view.checkResource(locale)) {
    	  view = null
      }

  	view

  }

}