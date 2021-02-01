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
package org.fusesource.scalate.wikitext

import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType

import collection.mutable.ListBuffer
import java.util.regex.{ Matcher, Pattern }
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.{ AbstractConfluenceDelimitedBlock, ParameterizedBlock }
import org.eclipse.mylyn.wikitext.core.parser.{ Attributes, TableAttributes, TableCellAttributes, TableRowAttributes }
import slogging.StrictLogging

class HtmlBlock extends AbstractConfluenceDelimitedBlock("html") {

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

abstract class AbstractNestedBlock(val name: String) extends ParameterizedBlock {
  var startPattern = Pattern.compile("\\s*\\{" + name + "(?::([^\\}]*))?\\}(.*)")
  var endPattern = Pattern.compile("(\\{" + name + "\\})(.*)")
  var blockLineCount: Int = 0
  var matcher: Matcher = null
  var nesting = false;

  override def processLineContent(line: String, ofs: Int): Int = {
    var end = line.length();
    var offset = ofs
    if (blockLineCount == 0) {
      setOptions(matcher.group(1))
      offset = matcher.start(2)
      beginBlock
      nesting = true
      end = offset
    } else {
      var terminating = false
      val endMatcher = endPattern.matcher(line)
      if (offset < end) {
        if (offset > 0) {
          endMatcher.region(offset, end)
        }
        if (endMatcher.find()) {
          terminating = true
          end = endMatcher.start(2)
        } else {
          end = offset
        }
      }
      if (terminating) {
        setClosed(true)
      }
    }
    blockLineCount = blockLineCount + 1
    var ret = -1
    if (end != line.length()) {
      ret = end
    }
    ret
  }

  override def findCloseOffset(line: String, lineOffset: Int): Int = {
    val endMatcher = endPattern.matcher(line)
    if (lineOffset != 0) {
      endMatcher.region(lineOffset, line.length())
    }
    if (endMatcher.find()) {
      endMatcher.start()
    } else {
      -1
    }
  }

  override def canStart(line: String, lineOffset: Int): Boolean = {
    blockLineCount = 0
    nesting = false
    matcher = startPattern.matcher(line)
    if (lineOffset > 0) {
      matcher.region(lineOffset, line.length())
    }
    matcher.matches()
  }

  override def setClosed(closed: Boolean) = {
    if (closed && !isClosed()) {
      endBlock()
    }
    super.setClosed(closed)
  }

  def beginBlock() = {
  }

  def endBlock() = {
  }

  override def beginNesting(): Boolean = nesting
}

class DivBlock extends AbstractNestedBlock("div") {
  var attributes = new Attributes()
  var textBuffer = new StringBuilder

  override def beginBlock() = {
    //attributes.setCssClass("syntax")
    builder.beginBlock(BlockType.DIV, attributes)
    super.beginBlock
  }

  override def endBlock() = {
    super.endBlock
    builder.endBlock()
    attributes = new Attributes()
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.setOption(attributes, key, value)
}

class SectionBlock extends AbstractNestedBlock("section") {
  var tableAttributes: TableAttributes = null
  var rowAttributes: TableRowAttributes = null
  var content = ListBuffer[String]()

  override def beginBlock() = {
    builder.beginBlock(BlockType.TABLE, tableAttributes)
    builder.beginBlock(BlockType.TABLE_ROW, rowAttributes)
    super.beginBlock
  }

  override def endBlock() = {
    super.endBlock
    builder.endBlock()
    builder.endBlock()
    content.clear
  }

  override def canStart(line: String, lineOffset: Int): Boolean = {
    val ret = super.canStart(line, lineOffset)
    if (ret) {
      defaultAttr
    }
    ret
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.setOption(tableAttributes, key, value)

  def defaultAttr() = {
    tableAttributes = new TableAttributes()
    tableAttributes.setCssClass("sectionMacro")
    tableAttributes.setBorder("0")
    tableAttributes.setCellpadding("5px")
    tableAttributes.setCellspacing("0")
    tableAttributes.setWidth("100%")
    rowAttributes = new TableRowAttributes()
  }
}

class ColumnBlock extends AbstractNestedBlock("column") {
  var attributes: TableCellAttributes = null

  override def beginBlock() = {
    builder.beginBlock(BlockType.TABLE_CELL_NORMAL, attributes)
    super.beginBlock
  }

  override def endBlock() = {
    super.endBlock
    builder.endBlock()
  }

  override def canStart(line: String, lineOffset: Int): Boolean = {
    val ret = super.canStart(line, lineOffset)
    if (ret) {
      defaultAttr
    }
    ret;
  }

  def defaultAttr() = {
    attributes = new TableCellAttributes()
    attributes.setCssClass("confluenceTd")
    attributes.setValign("top")
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.setOption(attributes, key, value)
}

class CenterBlock extends AbstractNestedBlock("center") {
  var attributes = new Attributes()

  override def beginBlock() = {
    attributes.setCssStyle("text-align: center;")
    builder.beginBlock(BlockType.DIV, attributes)
    super.beginBlock
  }

  override def endBlock() = {
    super.endBlock
    builder.endBlock()
    attributes = new Attributes()
  }

  override def setOption(option: String) =
    Blocks.unknownOption(option)

  override def setOption(key: String, value: String) =
    Blocks.setOption(attributes, key, value)
}

object Blocks extends StrictLogging {

  def unknownAttribute(key: String, value: String): Unit = {
    logger.warn("Unknown attribute '%s' with value: %s", key, value)
  }

  def unknownOption(option: String) = {
    logger.warn("Not sure how to set the option: %s", option)
  }

  def setOption(attributes: Attributes, key: String, value: String) = {
    key match {
      case "class" => attributes.setCssClass(value)
      case "style" => attributes.setCssStyle(value)
      case "id" => attributes.setId(value)
      case "lang" => attributes.setLanguage(value)
      case "title" => attributes.setTitle(value)
      case "width" => attributes match {
        case a: TableAttributes => a.setWidth(value)
        case a: TableCellAttributes => a.setWidth(value)
        case _ => unknownAttribute(key, value)
      }
      case _ => unknownAttribute(key, value)
    }
  }

}
