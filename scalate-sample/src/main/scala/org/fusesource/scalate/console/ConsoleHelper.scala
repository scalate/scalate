package org.fusesource.scalate.console


import _root_.java.io.{File, FileWriter}
import _root_.javax.servlet.{ServletConfig, ServletContext}
import _root_.javax.ws.rs.{GET, POST, QueryParam, Path}
import _root_.org.fusesource.scalate.servlet.{ServletRenderContext, ServletTemplateEngine}
import javax.ws.rs.core.Context
import java.util.{Set => JSet}
import scala.collection.mutable.Set
import scala.collection.JavaConversions._
import scala.xml.NodeSeq

/**
 * Helper snippets for creating the console
 *
 * @version $Revision : 1.1 $
 */
class ConsoleHelper(context: ServletRenderContext) {
  import context._

  // TODO figure out the viewName from the current template?
  def viewName = "index"

  /**
   * Returns the class name of the curren tresource
   */
  def resourceClassName: Option[String] = attributes.get("it") match {
    case Some(it: AnyRef) => Some(it.getClass.getName)
    case _ => None
  }

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

  /**
   * Creates the newly created template name if there can be one for the current resource
   */
  def newTemplateName(): Option[String] = resourceClassName match {
    case Some(resource) =>
      val prefix = resource + "."

      if (templates.exists(_.startsWith(prefix)) == false) {
        Some(prefix + viewName)
      }
      else {
        None
      }
    case _ => None
  }

  /**
   * Returns the current template names used in the current context
   */
  def templates: List[String] = attributes.get("scalateTemplates") match {
    case Some(list: List[String]) => list.removeDuplicates.sortWith(_<_)
    case _ => Nil
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