
package org.fusesource.scalate.guice

import _root_.com.google.inject.Guice
import _root_.com.google.inject.servlet.GuiceServletContextListener

/**
 * A servlet context listener which registers
 * <a href="http://code.google.com/p/google-guice/wiki/Servlets">Guice Servlet</a>
 *
 * @version $Revision: 1.1 $
 */
class ScalateGuiceContextListener extends GuiceServletContextListener {
  def getInjector = Guice.createInjector(new ScalateModule())
}