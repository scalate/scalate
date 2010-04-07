package ${package}

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

    // TODO add some custom provider methods here
    // which can then be injected into resources or templates
    //
    // @Provides def createSomething = new MyThing()

    // lets add any package names which contain JAXRS resources
    override def resourcePackageNames = "${package}.resources" :: super.resourcePackageNames
    

    // Note lets explicitly allow / to be served using a scaml template for now, remove this if you want a JAXRS
    // resource mapped to /
    //
    // TODO - we can remove this stuff when this Jersey issue is released
    // https://jersey.dev.java.net/issues/show_bug.cgi?id=485
    override def webPageContentRegex = "/" :: super.webPageContentRegex
  })
}
