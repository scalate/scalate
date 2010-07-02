package org.fusesource.scalate.test

import org.apache.commons.logging.LogFactory
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.resource.ResourceCollection
import org.fusesource.scalate.util.IOUtil
import java.io.{FileInputStream, File}
import java.lang.String

/**
 * @version $Revision : 1.1 $
 */
class JettyServer {
  @transient
  private final val LOG = LogFactory.getLog(classOf[JettyServer])

  val mavenWebAppSubDir = "src/main/webapp"

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
    val basedir = addFileSeparator(Config.baseDir)
    var defaultWebAppDir: String = basedir + mavenWebAppSubDir

    LOG.info("Using basedir: " + basedir)

    // we are typically used in a test so lets define the scalate test dir by default
    if (System.getProperty("scalate.workdir", "").length == 0) {
      System.setProperty("scalate.workdir", basedir + "target/scalate")
    }

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
      overlayWebAppDir = findOverlayModuleWebAppDir(basedir)
    }

    LOG.info("Defaulting the web app dir to: " + webAppDir + " with overlayDir: " + overlayWebAppDir)
    context.setContextPath(webAppContext)
    if (overlayWebAppDir != null) {
      def toUriString(name: String) = new File(name).getCanonicalFile.toURL.toString
      val array: Array[String] = Array(toUriString(webAppDir), toUriString(overlayWebAppDir))
      println("Using base resource URIs: " + array.mkString(" | "))
      context.setBaseResource(new ResourceCollection(array))
    }
    else {
      context.setResourceBase(webAppDir)
    }
    context.setServer(server)
    server.setHandler(context)
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

  protected def findOverlayModuleWebAppDir(basedir: String): String = {
    /**Lets walk up the directory tree looking for the overlayProject */
    def findOverlayModuleInParent(dir: String): String = exists(dir + "/" + overlayProject + "/" + mavenWebAppSubDir) match {
      case Some(file) => file.getAbsolutePath
      case _ => val parent = new File(dir).getParent
      if (parent == null || parent == dir) {
        null
      }
      else {
        findOverlayModuleInParent(parent)
      }
    }

    if (basedir.contains(overlayProject)) {null} else {
      var answer = findOverlayModuleInParent(basedir)
      if (answer == null) {
        // lets try find the WAR in the local repo
        val p = getClass.getPackage
        if (p == null) {
          LOG.warn("No package found for class: " + getClass.getName)
        }
        else {
          var version = p.getSpecificationVersion
          if (version == null) {
            version = p.getImplementationVersion
          }
          if (version == null) {
            LOG.warn("No version available for " + p)
          }
          else {
            val war = new File(System.getProperty("user.home", "~") + "/.m2/repository/org/fusesource/scalate/scalate-war/" + version + "/scalate-war-" + version + ".war")
            println("Looking for war at " + war.getAbsolutePath + " exists: " + war.exists)
            if (war.exists) {
              // lets extract the war to a temporary directory...
              // lets not do this if it already exists and the WAR is older than the directory!
              answer = basedir + "/target/scalate-war-overlay"
              val warDir = new File(answer)
              if (warDir.exists && warDir.lastModified <= war.lastModified) {
                println("Removing old expanded war " + warDir)
                IOUtil.recursiveDelete(warDir)
              }
              if (!warDir.exists) {
                println("Unpacking " + war + " to " + warDir)
                IOUtil.unjar(warDir, new FileInputStream(war), (!_.getName.matches("WEB-INF/(lib|_scalate).*")))
              }
            }
            else {
              LOG.warn("No scalate war found in local repo at " + war.getAbsolutePath)
            }
          }
        }
      }
      answer
    }
  }


  def rootUrl = "http://localhost:" + localPort + "/"

  def addFileSeparator(path: String) = if (path == null || path.length == 0) "" else path + "/"

  def exists(names: String*): Option[File] = names.map(new File(_)).find {f => f.exists}
}