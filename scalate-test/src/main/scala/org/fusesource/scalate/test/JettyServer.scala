package org.fusesource.scalate.test

import java.io.File

import org.apache.commons.logging.LogFactory
import org.mortbay.jetty.Connector
import org.mortbay.jetty.Handler
import org.mortbay.jetty.Server
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp.WebAppContext
import org.mortbay.resource.ResourceCollection

/**
 * @version $Revision : 1.1 $
 */

class JettyServer {
  @transient
  private final val LOG = LogFactory.getLog(classOf[JettyServer])

  val basedir = addFileSeparator(System.getProperty("basedir", ""))
  val mavenWebAppSubDir = "src/main/webapp"

  var defaultWebAppDir: String = basedir + mavenWebAppSubDir
  var defaultDirectory: String = "."

  /**
   * The overlay project used to add other resources for maven overlays
   */
  val overlayProject = "scalate-war"

  var overlayWebAppDir: String = _
  var server = new Server
  var port: Int = 0
  var localPort: Int = 0
  var webAppDir: String = _
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
      //webAppDir += "," + overlayWebAppDir
    }
    if (overlayWebAppDir == null) {
      overlayWebAppDir = findOverlayModuleWebAppDir
    }

    LOG.info("Defaulting the web app dir to: " + webAppDir + " with overlayDir: " + overlayWebAppDir)
    context.setContextPath(webAppContext)
    if (overlayWebAppDir != null) {
      context.setBaseResource(new ResourceCollection(Array(webAppDir, overlayWebAppDir)))
    }
    else {
      context.setResourceBase(webAppDir)
    }
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

  def findOverlayModuleWebAppDir: String = if (basedir.contains(overlayProject)) {null} else {
    exists(basedir + overlayProject + "/" + mavenWebAppSubDir, basedir + "../" + overlayProject + "/" + mavenWebAppSubDir) match {
      case Some(file) => file.getAbsolutePath
      case _ => null
    }
  }

  def rootUrl = "http://localhost:" + localPort + "/"


  def addFileSeparator(path: String) = if (path == null || path.length == 0) "" else path + "/"

  def exists(names: String*): Option[File] = names.map(new File(_)).find{f => val answer = f.exists; println("file " + f + " = " + answer); answer}
}