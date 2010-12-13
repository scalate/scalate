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

package org.fusesource.scalate.wikitext

import java.{util => ju}
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.AbstractConfluenceDelimitedBlock
import org.eclipse.mylyn.wikitext.core.parser.Attributes
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType
import org.fusesource.scalate.util.Logging
import collection.mutable.ListBuffer


class HtmlBlock extends AbstractConfluenceDelimitedBlock("html") with Logging {
  var div = false

  override def beginBlock() = {
    if (div) {
      val attributes = new Attributes()
      //attributes.setCssClass("syntax")
      builder.beginBlock(BlockType.DIV, attributes)
    }
  }


  override def handleBlockContent(value: String) = {
    builder.charactersUnescaped(value)
  }

  override def endBlock() = {
    if (div) {
      builder.endBlock()
    }
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.unknownAttribute(key, value)
}

class MarkupBlockBlock(blockType: BlockType, elementName: String) extends AbstractConfluenceDelimitedBlock(elementName) with Logging {
  var attributes = new Attributes()
  var textBuffer = new StringBuilder

  override def beginBlock() = {
    builder.beginBlock(blockType, attributes)
    textBuffer = new StringBuilder
  }


  override def handleBlockContent(value: String) = {
    if (!textBuffer.isEmpty) {
      textBuffer.append("\n")
    }
    textBuffer.append(value)
  }

  override def endBlock() = {
    // lets parse body as wiki markup at the end of the block
    getMarkupLanguage.processContent(getParser, textBuffer.toString, false)
    builder.endBlock()
    attributes = new Attributes()
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.setOption(attributes, key, value)
}


class DivBlock extends MarkupBlockBlock(BlockType.DIV, "div")

class ColumnBlock extends MarkupBlockBlock(BlockType.TABLE_CELL_NORMAL, "column")

class SectionBlock extends AbstractConfluenceDelimitedBlock("section") with Logging {
  var tableAttributes = new Attributes()
  var rowAttributes = new Attributes()
  var content = ListBuffer[String]()

  override def beginBlock() = {
    builder.beginBlock(BlockType.TABLE, tableAttributes)
    builder.beginBlock(BlockType.TABLE_ROW, rowAttributes)
  }

  override def handleBlockContent(value: String) = {
    getMarkupLanguage.processContent(getParser, value, false)
  }

  override def endBlock() = {
    builder.endBlock()
    builder.endBlock()
    tableAttributes = new Attributes()
    rowAttributes = new Attributes()
    content.clear
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.setOption(tableAttributes, key, value)
}


object Blocks extends Logging {
  def unknownAttribute(key: String, value: String): Unit = {
    warn("Unknown attribute '" + key + " with value: " + value)
  }

  def unknownOption(option: String) = {
    warn("Not sure how to set the option: " + option)
  }

  def setOption(attributes: Attributes, key: String, value: String) = {
    key match {
      case "class" => attributes.setCssClass(value)
      case "style" => attributes.setCssStyle(value)
      case "id" => attributes.setId(value)
      case "lang" => attributes.setLanguage(value)
      case "title" => attributes.setTitle(value)
      case _ => unknownAttribute(key, value)
    }
  }


}