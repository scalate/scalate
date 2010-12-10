/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import java.{util => ju, lang => jl}
import java.util.zip.ZipInputStream
import java.io.{FileInputStream, FileWriter, File, ByteArrayOutputStream}
import java.lang.StringBuilder
import org.apache.felix.gogo.commands.{Action, Option => option, Argument => argument, Command => command}
import org.osgi.service.command.CommandSession
import org.codehaus.swizzle.confluence.{Page, PageSummary, Confluence}
import collection.mutable.{HashMap, ListBuffer}
import org.fusesource.scalate.util.IOUtil

/**
 * <p>
 * Adding a tool that allows you to export confluence sites.  Example usage:
 *
 * <code>confexport --user user --password pass https://cwiki.apache.org/confluence/rpc/xmlrpc SM ./out</code>
 * 
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@command(scope = "scalate", name = "confexport", description = "Exports a confluence space.")
class ConfluenceExport extends Action {

  @argument(index = 0, required = true, name = "url", description = "URL to confluence RPC service")
  var url: String = "https://cwiki.apache.org/confluence/rpc/xmlrpc"
  @argument(index = 1, required = true, name = "space", description = "The confluence space key")
  var space: String = "SM"
  @argument(index = 2, required = false, name = "target", description = "The target directory")
  var target: File = new File(".")

  @option(name = "--user", description = "Login user id")
  var user: String = _
  @option(name = "--password", description = "Login password")
  var password: String = _
  @option(name = "--allow-spaces", description = "Whether to allow spaces in filenames (boolean). Only use if the O/S supports spaces in file names (e.g. on Windows). If false, file names are modified to be non-colonised names (NCName).")
  var allow_spaces: String = "false";
  @option(name = "--format", description = """The format of the downloaded pages. Possible values are:
page - for page files suitable for rendering in a Scalate web site. Suffix is .page
conf - for a plain confluence text file without metadata. Suffix is .conf""")
  var format: String = "page"

  case class Node(summary:PageSummary) {
    val children = ListBuffer[Node]()
  }

  def execute(session: CommandSession): AnyRef = {

    def println(value:Any) = session.getConsole.println(value)

    import JavaConversions._

    println("downloading space index...")
    val confluence = new Confluence(url);
    if( user!=null && password!=null ) {
      confluence.login(user, password);
    }
    val pageList = confluence.getPages(space).asInstanceOf[java.util.List[PageSummary]]

    var pageMap = Map( pageList.map(x=> (x.getId, Node(x))) : _ * )
    val rootNodes = ListBuffer[Node]()

    // add each node to the appropriate child collection.
    for( (key,node) <- pageMap ) {
      node.summary.getParentId match {
        case "0" => rootNodes += node
        case parentId => pageMap.get(parentId).foreach( _.children += node )
      }
    }

    def export(dir:File, nodes:ListBuffer[Node]):Int = {
      var rc = 0
      dir.mkdirs
      nodes.foreach { node=>
        val sanitized_title = sanitize(node.summary.getTitle);
        val page = confluence.getPage(node.summary.getId);
        var content:String = "";
        var file_suffix = ".page";
        if (format.equalsIgnoreCase("page")) {
            file_suffix = ".page"
            content = """---
title: """+page.getTitle+"""
page_version: """+page.getVersion+"""
page_creator: """+page.getCreator+"""
page_modifier: """+page.getModifier+"""
--- pipeline:conf
"""
        }
        else if (format.equalsIgnoreCase("conf")) {
            file_suffix = ".conf"
        }
        content += page.getContent

        val file = new File(dir, sanitized_title + file_suffix)
        println("downloading: \u001B[1;32m"+file+"\u001B[0m")
        IOUtil.writeText(file, content)
        rc += 1
        if( !node.children.isEmpty ) {
          rc += export(new File(dir, sanitized_title), node.children)
        }
      }
      rc
    }
    
    def sanitize(title:String): String = {
        if (allow_spaces.equalsIgnoreCase("true")) {
            title.replaceAll("\\\"", "")
        }
        else {
            title.toLowerCase.replaceAll(" ","-").replaceAll("[^a-zA-Z_0-9\\-\\.]", "")
        }
    }

    val total = export(target, rootNodes);
    println("Exported \u001B[1;32m%d\u001B[0m page(s)".format(total));
    null
  }



}
