package org.fusesource.scalate.console

import java.io.File
import scala.xml.NodeSeq

/**
 * @version $Revision : 1.1 $
 */

object EditLink {
  var idePluginPort = 51235

  def editLink(file: String)(body: => String): NodeSeq = editLink(file, None, None)(body)

  def editLink(file: String, line: Option[Int], col: Option[Int])(body: => String): NodeSeq = {
    System.getProperty("scalate.editor", "") match {
      case "textmate" => editLinkTextMate(file, line, col)(body)
      case "ide" => editLinkIdePlugin(file, line, col)(body)
      case "file" => editLinkFile(file, line, col)(body)
      case _ =>
        if (isMacOsx && hasTextMate)
          editLinkTextMate(file, line, col)(body)
        else {
          editLinkFile(file, line, col)(body)
        }
    }
  }

  def editLinkFile(file: String, line: Option[Int], col: Option[Int])(body: => String)  = {
    <a href={"file://" + file} title="Open File" target="_blank">{body}</a>
  }

  def editLinkTextMate(file: String, line: Option[Int], col: Option[Int])(body: => String) = {
    val href="txmt://open?url=file://" + file +
            (if (line.isDefined) "&line=" + line.get else "") +
            (if (col.isDefined) "&col=" + col.get else "")

    <a href={href} title="Open in TextMate">{body}</a>
  }

  def editLinkIdePlugin(file: String, line: Option[Int], col: Option[Int])(body: => String) = {
    val bodyText:String = body

    // The Atlassian IDE plugin seems to highlight the line after the actual line number, so lets subtract one
    val lineNumber = if (line.isDefined) {
      val n = line.get
      if (n > 1) n - 1 else 0
    }
    else 0

    <span>
      {bodyText}
      <img class="ide-icon tb_right_mid"
         id={"ide-" + file.hashCode}
         title={bodyText}
         onclick={"this.src='http://localhost:" + idePluginPort + "/file?file=" + file + "&line=" + lineNumber + "&id=' + Math.floor(Math.random()*1000);"}
         alt="Open in IDE"
         src={"http://localhost:" + idePluginPort + "/icon"}/>
    </span>
  }
  
  

  def isMacOsx = System.getProperty("os.name", "").contains("Mac OS X")

  def hasTextMate = exists("/Applications/TextMate.app") || exists("~/Applications/TextMate.app")

  def exists(fileName: String) = new File(fileName).exists
}