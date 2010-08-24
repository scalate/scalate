/**
 *  Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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
package org.fusesource.scalate.wikitext

import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage
import org.eclipse.mylyn.wikitext.core.parser.markup.Block
import java.util.List
import org.eclipse.mylyn.wikitext.core.parser.Attributes
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.{AbstractConfluenceDelimitedBlock, CodeBlock}
import java.lang.String
import collection.mutable.ListBuffer
import org.fusesource.scalate.util.IOUtil
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, OutputStream, InputStream}

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class PygementsConfluenceLanguage extends ConfluenceLanguage {

  override def addStandardBlocks(blocks: List[Block], paragraphBreakingBlocks: List[Block]) = {
    super.addStandardBlocks(blocks, paragraphBreakingBlocks)
    blocks.add(new PygementsCodeBlock)
    paragraphBreakingBlocks.add(new PygementsCodeBlock)
  }

  class PygementsCodeBlock extends AbstractConfluenceDelimitedBlock("pygmentize") {

	  var language:String = _
    var lines:Boolean = false

    var content = ListBuffer[String]()

    override def beginBlock() = {
      val attributes = new Attributes();
      attributes.setCssClass("syntax");
      builder.beginBlock(BlockType.DIV, attributes);
    }


    override def handleBlockContent(value:String) = {
      // collect all the content lines..
      content += value
    }

    override def endBlock() = {

      // To support indenting the macro.. we figure out the indent level of the
      // code block by looking at the indent of the last line
      val indent_re = """^([ \t]+)$""".r
      content.lastOption match {
        case Some(indent_re(indent)) =>
          // strip off those indents.
          content = content.map( _.replaceFirst("""^[ \t]{"""+indent.size+"""}""", "") )
        case _ =>
      }

      builder.charactersUnescaped(pygmentize(content.mkString("\n")));
      content.clear
      builder.endBlock();
    }

    override def setOption(option: String) = {
      language = option.toLowerCase();
    }

    override def setOption(key: String, value:String) = {
      key match {
        case "lines" => lines = value=="true"
        case "lang" => language = value
      }
    }

    def pygmentize(body:String) = {
      var options = "style=colorful"
      if( lines ) {
        options += ",linenos=1"
      }

      var process = Runtime.getRuntime.exec(Array("pygmentize", "-O", options, "-f", "html", "-l", language))
      thread("pygmetize err handler") {
        IOUtil.copy(process.getErrorStream, System.err)
      }

      val out = new ByteArrayOutputStream()
      thread("pygmetize out handler") {
        IOUtil.copy(process.getInputStream, out)
      }

      IOUtil.copy(new ByteArrayInputStream(body.getBytes), process.getOutputStream)
      process.getOutputStream.close

      process.waitFor
      if( process.exitValue != 0 ) {
        throw new RuntimeException("'pygmentize' execution failed: %d.  Did you install it from http://pygments.org/download/ ?".format(process.exitValue))
      }

      new String(out.toByteArray).replaceAll("""\r?\n""", "&#x000A;")
    }

    def thread(name:String)(func: =>Unit){
      new Thread(name) {
        override def run = {
          func
        }
      }.start()
    }

  }


}