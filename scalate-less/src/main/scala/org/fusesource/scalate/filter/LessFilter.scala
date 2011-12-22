/*
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
package org.fusesource.scalate.filter

import org.fusesource.scalate.{TemplateEngineAddOn, RenderContext, TemplateEngine}
import com.asual.lesscss.LessEngine

/**
 * Renders Less syntax.
 *
 * @author <a href="mailto:stuart.roebuck@gmail.com">Stuart Roebuck</a>
 */
object LessFilter extends Filter with TemplateEngineAddOn {

  private val lessEngine = new LessEngine

  def filter(context: RenderContext, content: String) = {
    synchronized {
      // This code block is synchronized as I'm not confident that the Less filter is thread safe.
      val css = lessEngine.compile(content).stripLineEnd
      """<style type="text/css">%n%s%n</style>""".format(css)
    }
  }

  /**
   * Add the less filter to the template engine.
   */
  def apply(te: TemplateEngine) {
    te.filters += "less" -> LessFilter
    te.pipelines += "less" -> List(LessFilter)
  }
}
