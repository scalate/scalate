package org.fusesource.scalate.console

import _root_.javax.servlet.ServletContext
import _root_.org.fusesource.scalate.servlet.ServletRenderContext
import java.io.File
import scala.xml.NodeSeq

/**
 * @version $Revision: 1.1 $
 */

trait ConsoleSnippets {

  def servletContext: ServletContext

  def renderContext: ServletRenderContext

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


  def shorten(file: File): String = shorten(file.getPath)

  def shorten(file: String): String = {
    var root = renderContext.engine.workingDirectory.getPath;
    if (file.startsWith(root)) {
      file.substring(root.length + 1)
    } else {
      sourcePrefixes.find(file.startsWith(_)) match {
        case Some(prefix) => file.substring(prefix.length + 1)
        case _ => file
      }
    }
  }

  protected var sourcePrefixes = List("src/main/scala", "src/main/java")
}