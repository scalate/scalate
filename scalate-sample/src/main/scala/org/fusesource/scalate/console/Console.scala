package org.fusesource.scalate.console

import _root_.java.io.{File, FileWriter}
import _root_.javax.servlet.{ServletConfig, ServletContext}
import _root_.javax.ws.rs.{GET, POST, QueryParam, Path}
import _root_.org.fusesource.scalate.servlet.ServletTemplateEngine
import javax.ws.rs.core.Context
import java.util.{Set => JSet}
import scala.collection.mutable.Set
import scala.collection.JavaConversions._
import scala.xml.NodeSeq


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
  var _servletContext: ServletContext = _

  def servletContext: ServletContext = {
    if (_servletContext == null) {
      throw new NullPointerException("servletContext not injected")
    }
    _servletContext
  }

  @Context
  var _servletConfig: ServletConfig = _

  def servletConfig: ServletConfig = {
    if (_servletConfig == null) {
      throw new NullPointerException("servletConfig not injected")
    }
    _servletConfig
  }

  @POST
  @Path("createTemplate")
  def createTemplate(@QueryParam("name") newTemplateName: String, @QueryParam("archetype") archetype: String) = {
    // lets evaluate the archetype and write the output to the given template name
    val fileName = servletContext.getRealPath(newTemplateName)
    if (fileName == null) {
      throw new IllegalArgumentException("Could not deduce real file name for: " + newTemplateName)
    }
    val engine = new ServletTemplateEngine(servletConfig)
    val text = engine.layout(archetype)
    val out = new FileWriter(fileName)
    out.write(text)
    out.close
  }

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
    val dir = "/WEB-INF/archetypes/" + viewName
    var files: Array[File] = Array()
    val fileName = servletContext.getRealPath(dir)
    if (fileName != null) {
      val file = new File(fileName)
      if (file.exists && file.isDirectory) {
        files = file.listFiles
      }
    }
    files.map(new Archetype(_))
  }

  @GET
  @Path("archetypes")
  def archetypesText = archetypes.mkString(", ")

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

  /**
   * returns an edit link for the given URI, discovering the right URL
   * based on your OS and whether you have TextMate installed and whether you
   * have defined the <code>scalate.editor</code> system property
   */
  def editLink(template: String)(body: => Unit): NodeSeq = editLink(template, None, None)(body)

    /**
     * returns an edit link for the given URI, discovering the right URL
     * based on your OS and whether you have TextMate installed and whether you
     * have defined the <code>scalate.editor</code> system property
     */
  def editLink(template: String, line: Option[Int], col: Option[Int])(body: => Unit): NodeSeq = {
    val file = servletContext.getRealPath(template)
    EditLink.editLink(file, line, col)(body)
  }
}