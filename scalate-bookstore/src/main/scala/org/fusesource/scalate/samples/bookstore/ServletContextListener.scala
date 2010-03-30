package org.fusesource.scalate.samples.bookstore

import _root_.com.google.inject.Guice
import _root_.com.google.inject.servlet.GuiceServletContextListener
import _root_.org.fusesource.scalate.guice.ScalateModule

/**
 * A servlet context listener which registers
 * <a href="http://code.google.com/p/google-guice/wiki/Servlets">Guice Servlet</a>
 *
 * @version $Revision: 1.1 $
 */

class ServletContextListener extends GuiceServletContextListener {
  def getInjector = Guice.createInjector(new ScalateModule() {

    // TODO add some custom providers here...

    override def resourcePackageNames = "org.fusesource.scalate.samples.bookstore.resources" :: super.resourcePackageNames
  })
}
