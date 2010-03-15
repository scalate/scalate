package org.fusesource.scalate.console

import _root_.java.io.File
import javax.servlet.ServletContext
import javax.ws.rs.core.Context
import java.util.{Set => JSet}
import javax.ws.rs.{QueryParam, Path}
import scala.collection.mutable.Set
import scala.collection.JavaConversions._



/**
 * The Scalate development console
 *
 * @version $Revision : 1.1 $
 */
@Path("/scalate")
class Console extends DefaultRepresentations {

  @QueryParam("r")
  var resource: String = _

  @QueryParam("t")
  var _templates: JSet[String] = _

  @Context
  var servletContext: ServletContext = _


  def templates: Set[String] = {
    if (_templates != null) {
      asSet(_templates)
    }
    else {
      Set()
    }
  }

  // TODO figure out the viewName from the current template?
  def viewName = "index"

  /**
   * Returns all the available archetypes for the current view name
   */
  def archetypes: Array[Archetype] = {
    val dir = "/WEB-INF/" + viewName
    if (servletContext == null) {
      throw new NullPointerException("servletContext not injected")
    }
    var files: Array[File] = Nil
    val fileName = servletContext.getRealPath(dir)
    if (fileName != null) {
      val file = new File(fileName)
      if (file.exists && file.isDirectory) {
        files = file.listFiles
      }
    }
    files.map(new Archetype(_))
  }


  def newTemplateName: Option[String] = {
    if (resource != null) {
      val prefix = resource + "."

      if (templates.exists(_.startsWith(prefix)) == false) {
        Some(prefix + viewName)
      }
      else {
        None
      }
    }
    None
  }
}