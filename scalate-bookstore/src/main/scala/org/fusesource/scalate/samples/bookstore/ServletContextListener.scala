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
