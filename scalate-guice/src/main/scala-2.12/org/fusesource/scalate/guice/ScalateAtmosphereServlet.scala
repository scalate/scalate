package org.fusesource.scalate.guice

import javax.servlet.ServletConfig
import com.google.inject.{ Inject, Injector, Singleton }
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import org.atmosphere.cpr.AtmosphereServlet
import org.atmosphere.handler.ReflectorServletProcessor
import org.atmosphere.cpr.FrameworkConfig.JERSEY_BROADCASTER
import org.atmosphere.cpr.ApplicationConfig.PROPERTY_SERVLET_MAPPING

/**
 * Atmosphere servlet configured by Scalate/Guice
 *
 * @author Sven Jacobs <mail@svenjacobs.com>
 */
@Singleton
class ScalateAtmosphereServlet @Inject() (
  private[this] val injector: Injector) extends AtmosphereServlet {

  /**
   * Most of the code to set up AtmosphereServlet has been borrowed from
   * {@link org.atmosphere.guice.AtmosphereGuiceServlet#detectSupportedFramework}
   */
  override def detectSupportedFramework(sc: ServletConfig): Boolean = {
    import AtmosphereServlet._

    setDefaultBroadcasterClassName(JERSEY_BROADCASTER)

    val guiceContainer = injector.getInstance(classOf[GuiceContainer])

    val rsp = new ReflectorServletProcessor()
    rsp.setServlet(guiceContainer)

    var mapping = sc.getInitParameter(PROPERTY_SERVLET_MAPPING)
    if (mapping == null) mapping = "/*"

    getAtmosphereConfig.setSupportSession(false)
    addAtmosphereHandler(mapping, rsp)

    true
  }
}
