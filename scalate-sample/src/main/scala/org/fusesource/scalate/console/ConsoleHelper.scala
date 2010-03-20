package org.fusesource.scalate.console

import _root_.org.fusesource.scalate.RenderContext
import org.fusesource.scalate.servlet.ServletRenderContext
import org.fusesource.scalate.util.Logging
import java.io.File
import scala.io.Source
import scala.xml.{Text, NodeSeq}
import collection.mutable.{ArrayBuffer, ListBuffer}
import util.parsing.input.{Position, OffsetPosition}

case class SourceLine(line: Int, source: String) {
  def style(errorLine: Int): String = if (line == errorLine) "line error" else "line"

  def nonBlank = source != null && source.length > 0
}

/**
 * Helper snippets for creating the console
 *
 * @version $Revision : 1.1 $
 */
class ConsoleHelper(context: ServletRenderContext) extends Logging {
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

  def editLink(template: String, line: Int, col: Int)(body: => Unit): NodeSeq = editLink(template, Some(line), Some(col))(body)

  /**
   * returns an edit link for the given URI, discovering the right URL
   * based on your OS and whether you have TextMate installed and whether you
   * have defined the <code>scalate.editor</code> system property
   */
  def editLink(filePath: String, line: Option[Int], col: Option[Int])(body: => Unit): NodeSeq = {
    // It might be a real file path
    val file = new File(filePath);
    val realPath = if( file.exists ) {
      file.getCanonicalPath
    } else {
      servletContext.getRealPath(filePath)
    }
    EditLink.editLink(realPath, line, col)(body)
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


  /**
   * Retrieves a chunk fo lines either side of the given error line
   */
  def lines(template: String, errorLine: Int, chunk: Int): Seq[SourceLine] = {
    val file = servletContext.getRealPath(template)
    if (file != null) {
      val source = Source.fromPath(file)
      val start = (errorLine - chunk).min(0)
      val end = start + chunk

      val list = new ListBuffer[SourceLine]
      for (i <- 1.to(end)) {
        val code = source.getLine(1)
        if (i >= start) {
          list += SourceLine(i, code)
        }
      }
      list
      /*
            // lets strip the head and tail blank items (TODO there must be an easier way??)
            val from = seq.indexWhere(_.nonBlank) - 1
            if (from > 0) {
              seq = seq.drop(from)
            }
            val to = seq.lastIndexWhere(_.nonBlank) + 1
            if (to > 0) {
              seq = seq.take(to)
            }
      seq
      */
    }
    else {
      Nil
    }
  }

  /**
   * Retrieves a chunk of lines either side of the given error line
   */
  def lines(template: String, pos: Position, chunk: Int = 5): Seq[SourceLine] = {
    pos match {
      case op:OffsetPosition=>

        // OffsetPosition's already are holding onto the file contents
        val index: Array[String] = {
          val source = op.source
          var rc = new ArrayBuffer[String]
          var start = 0;
          for (i <- 0 until source.length) {
            if (source.charAt(i) == '\n') {
              rc += source.subSequence(start, i).toString.stripLineEnd
              start = i + 1
            }
          }
          rc.toArray
        }


        val start = (pos.line - chunk).max(1)
        val end = (pos.line + chunk).min(index.length)

        val list = new ListBuffer[SourceLine]
        for (i <- start to end) {
          list += SourceLine(i, index(i-1))
        }
        list


      case _=>

        // We need to manually load the file..
        lines(template, pos.line, chunk)
    }

  }

  def shorten(file: String): String = {
    var root = RenderContext().engine.workingDirectory.getPath;
    if( file.startsWith(root) ) {
      file.substring(root.length +1 )
    } else {
      file
    }
  }

}