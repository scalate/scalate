package org.fusesource.scalate.samples.scuery

import _root_.com.google.inject.Guice
import _root_.com.google.inject.servlet.GuiceServletContextListener
import _root_.org.fusesource.scalate.guice.ScalateModule
import _root_.javax.servlet.{ServletContext, ServletContextEvent}

object ServletContextListener {
  private var _servletContext: ServletContext = _

  def apply(): ServletContext = _servletContext

  def update(servletContext: ServletContext): Unit = { _servletContext = servletContext}
}

/**
 * A servlet context listener which registers
 * <a href="http://code.google.com/p/google-guice/wiki/Servlets">Guice Servlet</a>
 *
 * @version $Revision : 1.1 $
 */
class ServletContextListener extends GuiceServletContextListener {

  override def contextInitialized(event: ServletContextEvent) = {
    super.contextInitialized(event)
    ServletContextListener.update(event.getServletContext())
  }

  def getInjector = Guice.createInjector(new ScalateModule() {

    // TODO add some custom provider methods here
    // which can then be injected into resources or templates
    //
    // @Provides def createSomething = new MyThing()

    // lets add any package names which contain JAXRS resources
    // https://jersey.dev.java.net/issues/show_bug.cgi?id=485
    override def resourcePackageNames = "org.fusesource.scalate.samples.scuery.resources" :: super.resourcePackageNames
  })
}
