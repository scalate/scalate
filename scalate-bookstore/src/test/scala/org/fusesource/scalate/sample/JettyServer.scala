package org.fusesource.scalate.sample

import java.io.File

import org.apache.commons.logging.LogFactory
import org.mortbay.jetty.Connector
import org.mortbay.jetty.Handler
import org.mortbay.jetty.Server
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp.WebAppContext

/**
 * @version $Revision : 1.1 $
 */

class JettyServer {
  @transient
  private final val LOG = LogFactory.getLog(classOf[JettyServer])

  var defaultWebAppDir: String = "src/main/webapp"
  var defaultDirectory: String = "scalate-bookstore"

  var server = new Server
  var port: Int = 0
  var localPort: Int = 0
  var webAppDir: String = null
  var webAppContext: String = "/"


  def start: Unit = {
    LOG.info("Starting Web Server on port: " + port)
    var connector = new SelectChannelConnector
    connector.setPort(port)
    connector.setServer(server)

    var context = new WebAppContext
    if (webAppDir == null) {
      var file: File = new File(defaultWebAppDir)
      if (file.exists) {
        webAppDir = defaultWebAppDir
      }
      else {
        webAppDir = defaultDirectory + "/" + defaultWebAppDir
      }
      LOG.info("Defaulting the web app dir to: " + webAppDir)
    }
    context.setResourceBase(webAppDir)
    context.setContextPath(webAppContext)
    context.setServer(server)
    server.setHandlers(Array[Handler](context))
    server.setConnectors(Array[Connector](connector))
    server.start

    localPort = connector.getLocalPort

    LOG.info("==============================================================================")
    LOG.info("Started the Web Server: point your web browser at " + rootUrl)
    LOG.info("==============================================================================")
  }


  def stop: Unit = {
    server.stop
  }

  def rootUrl = "http://localhost:" + localPort + "/"
}