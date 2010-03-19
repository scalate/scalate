package org.fusesource.scalate.console

import java.io.File
import org.fusesource.scalate.servlet.ServletRenderContext
import scala.xml.NodeSeq


/**
 * Helper snippets for creating the console
 *
 * @version $Revision : 1.1 $
 */
class ConsoleHelper(context: ServletRenderContext) {
  import context._

  val consoleParameter = "_scalate"

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
   * Returns an attempt at finding the source file for the current resource.
   *
   * TODO use bytecode swizzling to find the accurate name from the debug info in
   * the class file!
   */
  def resourceSourceFile: Option[File] = resourceClassName match {
    case Some(name: String) =>
      val fileName = name.replace('.', '/')
      val prefixes = List("src/main/scala/", "src/main/java/")
      val postfixes = List(".scala", ".java")

      val names = for (prefix <- prefixes; postfix <- postfixes) yield new File(prefix + fileName + postfix)
      names.find(_.exists)

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
    files.map(f => new Archetype(new File(dir, f.getName)))
  }

  /**
   * Creates the newly created template name if there can be one for the current resource
   */
  def newTemplateName(): Option[String] = resourceClassName match {
    case Some(resource) =>
      val prefix = "/" + resource.replace('.', '/') + "."

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
    case Some(list: List[String]) => list.removeDuplicates.sortWith(_ < _)
    case _ => Nil
  }

  /**
   * Returns the current layouts used in the current context
   */
  def layouts: List[String] = attributes.get("scalateLayouts") match {
    case Some(list: List[String]) => list.removeDuplicates.sortWith(_ < _)
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

  /**
   * returns an edit link for the given file, discovering the right URL
   * based on your OS and whether you have TextMate installed and whether you
   * have defined the <code>scalate.editor</code> system property
   */
  def editFileLink(template: String)(body: => Unit): NodeSeq = editFileLink(template, None, None)(body)

  /**
   * returns an edit link for the given file, discovering the right URL
   * based on your OS and whether you have TextMate installed and whether you
   * have defined the <code>scalate.editor</code> system property
   */
  def editFileLink(file: String, line: Option[Int], col: Option[Int])(body: => Unit): NodeSeq = {
    EditLink.editLink(file, line, col)(body)
  }


  /**
   * Returns true if the option is enabled
   */
  def optionEnabled(name: String): Boolean = parameterValues(consoleParameter).contains(name)

  /**
   * Link to the current page with the option enabled
   */
  def enableLink(name: String): String = currentUriPlus(consoleParameter + "=" + name)

  /**
   * Link to the current page with the option disabled
   */
  def disableLink(name: String): String = currentUriMinus(consoleParameter + "=" + name)
}