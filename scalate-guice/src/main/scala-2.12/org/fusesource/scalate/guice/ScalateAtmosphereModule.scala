package org.fusesource.scalate.guice

import collection.JavaConverters._

/**
 * ScalateModule that also configures the Atmosphere framework
 * which here acts as an extension to Jersey for developing
 * Comet/WebSocket resources.
 *
 * @author Sven Jacobs <mail@svenjacobs.com>
 */
class ScalateAtmosphereModule extends ScalateModule {

  override def configureServlets(): Unit = {
    // We don't need to call super.applyJerseyFilter here because Atmosphere
    // will configure / is using Jersey
    applyScalateServlets
    applyAtmosphereServlets()
  }

  protected def applyAtmosphereServlets(): Unit = {
    val props: Map[String, String] = (for ((name, value) <- createResourceConfigProperties) yield (name, value.toString)) ++ Map(
      "org.atmosphere.useWebSocket" -> "true",
      "org.atmosphere.useNative" -> "true")

    // This regex matches everything except when the URI ends with common file extensions (.js, .css, etc).
    // The outer group is required by Guice's servlet configuration and matches the whole URI
    val regex = "(^/.*(?<!\\.%s)$)" format fileExtensionsExcludedFromJersey.mkString("(?:", "|", ")")
    serveRegex(regex).`with`(classOf[ScalateAtmosphereServlet], props.asJava)
  }
}