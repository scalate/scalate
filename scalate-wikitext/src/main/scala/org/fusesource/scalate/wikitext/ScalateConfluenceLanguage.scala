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

import org.eclipse.mylyn.wikitext.core.parser.markup.Block
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage
import java.{util => ju}

/**
 * Adds extendsions to the Confluence language such as support for a 'pygmentize' macro
 *
 * The pygmentize macro will use the pygmentize command line tool to syntax highlight the code within the block
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ScalateConfluenceLanguage extends ConfluenceLanguage {
  override def addStandardBlocks(blocks: ju.List[Block], paragraphBreakingBlocks: ju.List[Block]) = {
    super.addStandardBlocks(blocks, paragraphBreakingBlocks)

    List(new PygementsBlock, new SnippetBlock, new IncludeBlock,
      new HtmlBlock, new DivBlock,
      new SectionBlock, new ColumnBlock
    ).foreach{b =>
      blocks.add(b)
      paragraphBreakingBlocks.add(b)
    }
  }
}