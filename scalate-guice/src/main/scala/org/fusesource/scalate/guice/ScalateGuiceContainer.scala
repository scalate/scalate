package org.fusesource.scalate.guice

import _root_.com.google.inject.Injector
import _root_.com.sun.jersey.api.core.ResourceConfig
import _root_.com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import _root_.com.sun.jersey.spi.container.servlet.WebConfig
import _root_.com.sun.jersey.spi.container.WebApplication
import _root_.java.util.{Map => JMap}
import _root_.org.fusesource.scalate.util.Logging

/**
 *
 * @version $Revision: 1.1 $
 */
class ScalateGuiceContainer(injector: Injector) extends GuiceContainer(injector) with Logging {

  // TODO should the GuiceContainer class do this too?
  override def getDefaultResourceConfig(props: JMap[String,AnyRef], wc: WebConfig):ResourceConfig = injector.getInstance(classOf[ResourceConfig])


  override def initiate(rc: ResourceConfig, wa: WebApplication) = {
    debug("container created with " + rc + " properties: " + rc.getProperties)
    super.initiate(rc, wa)
  }
}