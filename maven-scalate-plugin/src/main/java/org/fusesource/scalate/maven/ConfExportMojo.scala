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

package org.fusesource.scalate
package maven

import collection.JavaConversions._

import java.util.zip.ZipInputStream
import java.io.{FileInputStream, FileWriter, File, ByteArrayOutputStream}
import java.lang.StringBuilder
import org.codehaus.swizzle.confluence.{Page, PageSummary, Confluence}
import collection.mutable.{HashMap, ListBuffer}
import org.fusesource.scalate.util.IOUtil

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.project.MavenProject

import org.scala_tools.maven.mojo.annotations._

/**
 * This goal exports confluence mark-up out of a Confluence wiki and adds the files to 
 * the resource target for Scalate to use in generating the site. Its guts are 
 * copied from the ConfluenceExport command. This should be made more 
 * modular.
 *
 * @author Eric Johnson
 */
@goal("conf-export")
@phase("generate-sources")
@executeGoal("conf-export")
@executePhase("generate-sources")
@requiresProject
class ConfExportMojo extends AbstractMojo {
  @parameter
  @expression("${project}")
  @readOnly
  @required
  var project: MavenProject = _

  @parameter
  @required
  @description("URL to confluence RPC service")
  var url: String = "https://cwiki.apache.org/confluence/rpc/xmlrpc"

  @parameter
  @required
  @description("The confluence space key")
  var space: String = "SM"

  @parameter
  @description("The target directory")
  var target: File = new File(".")

  @parameter
  @description("The Confluence username to access the wiki.")
  var user : String = _

  @parameter
  @description("The password used to access the wiki.")
  var password : String = _

  @parameter
  @description("Whether to allow spaces in filenames (boolean)")
  var allow_spaces: String = "false"
  
  @parameter
  @description("The format of the downloaded pages. Possible values are: page and conf")
  var format: String = "page"

  @parameter
  @description("The directory where the exported pages will land.")
  @expression("${project.build.directory}/generated-sources/confluence")
  var targetDirectory: File = _
  
  case class Node(summary:PageSummary) {
    val children = ListBuffer[Node]()
  }

  def execute() = {
    targetDirectory.mkdirs();

    getLog.info("Extracting pages from " + url + "/" + space);

    getLog.info("downloading space index...")
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
        getLog.info("downloading: \u001B[1;32m"+file+"\u001B[0m")
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
    getLog.info("Exported \u001B[1;32m%d\u001B[0m page(s)".format(total));
    null
  }

}