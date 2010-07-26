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

package org.fusesource.scalate.samples.bookstore

import _root_.com.google.inject.servlet.GuiceServletContextListener
import _root_.org.fusesource.scalate.guice.ScalateModule
import com.google.inject.{Provides, Guice}
import service.FooService

/**
 * A servlet context listener which registers
 * <a href="http://code.google.com/p/google-guice/wiki/Servlets">Guice Servlet</a>
 *
 * @version $Revision : 1.1 $
 */

class ServletContextListener extends GuiceServletContextListener {

  def getInjector = Guice.createInjector(new ScalateModule() {

    @Provides
    def foo = new FooService {
      def name = "MyFoo"
    }

    // lets add any package names which contain JAXRS resources
    // https://jersey.dev.java.net/issues/show_bug.cgi?id=485
    override def resourcePackageNames = "org.fusesource.scalate.samples.bookstore.resources" :: super.resourcePackageNames
  })
}
