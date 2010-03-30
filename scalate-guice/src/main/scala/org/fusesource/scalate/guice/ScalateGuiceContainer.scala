package org.fusesource.scalate.guice

import _root_.com.google.inject.Injector
import _root_.com.sun.jersey.api.core.ResourceConfig
import _root_.com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import _root_.com.sun.jersey.spi.container.servlet.WebConfig
import _root_.com.sun.jersey.spi.container.WebApplication
import _root_.java.util.{Map => JMap}

/**
 *
 * @version $Revision: 1.1 $
 */
class ScalateGuiceContainer(injector: Injector) extends GuiceContainer(injector) {

  // TODO should the GuiceContainer class do this too?
  override def getDefaultResourceConfig(props: JMap[String,AnyRef], wc: WebConfig):ResourceConfig = injector.getInstance(classOf[ResourceConfig])


  override def initiate(rc: ResourceConfig, wa: WebApplication) = {
    println("ScalateGuiceContainer.initiate with " + rc + " properties: " + rc.getProperties)
    super.initiate(rc, wa)
  }
}