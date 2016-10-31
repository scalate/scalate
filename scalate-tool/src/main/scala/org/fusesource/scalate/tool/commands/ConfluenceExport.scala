/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.tool.commands

import collection.JavaConversions

import java.{ util => ju, lang => jl }
import java.util.zip.ZipInputStream
import java.io.{ FileInputStream, FileWriter, File, ByteArrayOutputStream }
import java.lang.StringBuilder
import org.apache.felix.gogo.commands.{ Action, Option => option, Argument => argument, Command => command }
import org.apache.felix.service.command.CommandSession
import org.swift.common.soap.confluence.{ RemotePage, RemotePageSummary, ConfluenceSoapService, ConfluenceSoapServiceServiceLocator }
import collection.mutable.{ HashMap, ListBuffer }
import org.fusesource.scalate.util.IOUtil

/**
 * <p>
 * Adding a tool that allows you to export confluence sites.  Example usage:
 *
 * <code>confexport --user user --password pass https://cwiki.apache.org/confluence SM ./out</code>
 *
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@command(scope = "scalate", name = "confexport", description = "Exports a confluence space.")
class ConfluenceExport extends Action {

  @argument(index = 0, required = true, name = "url", description = "The confluence base URL (e.g. https://cwiki.apache.org/confluence)")
  var url: String = "https://cwiki.apache.org/confluence"
  @argument(index = 1, required = true, name = "space", description = "The confluence space key")
  var space: String = "SM"
  @argument(index = 2, required = false, name = "target", description = "The target directory")
  var target: File = new File(".")

  @option(name = "--user", description = "Login user id")
  var user: String = _
  @option(name = "--password", description = "Login password")
  var password: String = _
  @option(name = "--allow-spaces", description = "(no arg) Allow spaces and other irregular chars in filenames. Use only if the O/S supports spaces in file names (e.g. Windows).")
  var allow_spaces: Boolean = false
  @option(name = "--format", description = """The format of the downloaded pages. Possible values are:
page - for page files suitable for rendering in a Scalate web site. Suffix is .page
conf - for a plain confluence text file without metadata. Suffix is .conf""")
  var format: String = "page"
  @option(name = "--target-db", description = "(no arg) Generate a link database for DocBook.")
  var target_db: Boolean = false

  case class Node(summary: RemotePageSummary) {
    val children = ListBuffer[Node]()
  }

  def execute(session: CommandSession): AnyRef = {
    def println(value: Any) = session.getConsole.println(value)
    this.execute(value => println(value))
  }

  def execute(println: String => Unit): AnyRef = {

    import JavaConversions._

    println("downloading space index...")
    var confluence = serviceSetup(url + defaultConfluenceServiceExtension)
    if (user != null && password != null) {
      loginToken = confluence.login(user, password);
    }
    val pageList: java.util.List[RemotePageSummary] = confluence.getPages(loginToken, space).toList

    var pageMap = Map(pageList.map(x => (x.getId, Node(x))): _*)
    val rootNodes = ListBuffer[Node]()

    // add each node to the appropriate child collection.
    for ((key, node) <- pageMap) {
      node.summary.getParentId match {
        case 0 => rootNodes += node
        case parentId => pageMap.get(parentId).foreach(_.children += node)
      }
    }

    def export(dir: File, nodes: ListBuffer[Node]): Int = {
      var rc = 0
      dir.mkdirs
      nodes.foreach { node =>
        val sanitized_title = sanitize(node.summary.getTitle);
        val page = confluence.getPage(loginToken, node.summary.getId);
        var content: String = "";
        var file_suffix = ".page";
        if (format.equalsIgnoreCase("page")) {
          file_suffix = ".page"
          content = """---
title: """ + page.getTitle + """
page_version: """ + page.getVersion + """
page_creator: """ + page.getCreator + """
page_modifier: """ + page.getModifier + """
--- pipeline:conf
"""
        } else if (format.equalsIgnoreCase("conf")) {
          file_suffix = ".conf"
        }
        content += page.getContent

        val file = new File(dir, sanitized_title + file_suffix)
        println("downloading: \u001B[1;32m" + file + "\u001B[0m")
        IOUtil.writeText(file, content)
        rc += 1
        if (target_db) {
          TargetDB.startDiv(
            sanitized_title, // targetptr (used in DocBook olinks)
            page.getTitle // Page title
          )
        }
        if (!node.children.isEmpty) {
          rc += export(new File(dir, sanitized_title), node.children)
        }
        if (target_db) {
          TargetDB.endDiv()
        }
      }
      rc
    }

    def sanitize(title: String): String = {
      if (allow_spaces) {
        title.replaceAll("\\\"", "")
      } else {
        title.toLowerCase.replaceAll(" ", "-").replaceAll("[^a-zA-Z_0-9\\-\\.]", "")
      }
    }

    if (target_db) {
      TargetDB.rootDir = target
      TargetDB.init(space, space)
    }
    val total = export(target, rootNodes);
    if (target_db) {
      TargetDB.close()
    }
    println("Exported \u001B[1;32m%d\u001B[0m page(s)".format(total));
    confluence.logout(loginToken)
    null
  }

  //-----
  // Definitions to set up the Confluence SOAP-RPC service
  //

  var confluence: ConfluenceSoapService = null
  var loginToken: String = null
  val defaultConfluenceServiceExtension = "/rpc/soap-axis/confluenceservice-v1" // Confluence soap service

  def serviceSetup(address: String): ConfluenceSoapService = {
    var serviceLocator = new ConfluenceSoapServiceServiceLocator
    serviceLocator.setConfluenceserviceV2EndpointAddress(address);
    serviceLocator.getConfluenceserviceV2();
  }

  //-----
  // The TargetDB object enables the generation of a DocBook
  // link database, target.db. Using the link database, a DocBook
  // document can then easily cross-reference a confluence page.

  object TargetDB {
    var rootDir = new File(".")
    var rootFileName = "index.html"
    var targetDBFileName = "target.db"
    var level: Int = 0
    var targetContent: StringBuffer = new StringBuffer
    var hrefStack = new java.util.Stack[String]

    def init(targetptr: String, title: String) {
      targetContent.append(
        "<div element=\"book\" href=\"" + rootFileName
          + "\" number=\"\" targetptr=\""
          + targetptr + "\">\n"
          + "    <ttl>" + title + "</ttl>\n"
          + "    <xreftext>" + title + "</xreftext>\n"
      )
      level = 1
    }

    def close() {
      while (level > 0) { endDiv() }
      val file = new File(rootDir, targetDBFileName)
      IOUtil.writeText(file, targetContent.toString())
    }

    def startDiv(targetptr: String, title: String) {
      val indent = "    " * level
      var escTitle = escapeXml(title)
      appendAndPush(hrefStack, targetptr)
      var href = hrefStack.peek + ".html"
      targetContent.append(
        indent + "<div element=\"" + getDivElementName() + "\" href=\""
          + href + "\" number=\"\" targetptr=\""
          + targetptr + "\">\n"
          + indent + "    <ttl>" + escTitle + "</ttl>\n"
          + indent + "    <xreftext>" + escTitle + "</xreftext>\n"
      )
      level += 1
    }

    def endDiv() {
      level -= 1
      if (!hrefStack.empty) hrefStack.pop
      targetContent.append("    " * level + "</div>\n")
    }

    protected def getDivElementName(): String = {
      if (level == 0) { "book" }
      else if (level == 1) { "chapter" }
      else { "section" }
    }

    protected def escapeXml(text: String): String = {
      text.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
    }

    protected def appendAndPush(stack: java.util.Stack[String], segment: String) {
      if (stack.empty) {
        stack.push(segment)
      } else {
        stack.push(stack.peek() + "/" + segment)
      }
    }

  }

}
