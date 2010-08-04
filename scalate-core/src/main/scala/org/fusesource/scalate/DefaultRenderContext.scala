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

import _root_.org.fusesource.scalate.util.{RenderHelper, Logging}
import _root_.org.fusesource.scalate.support.{AttributesHashMap}
import java.io._
import collection.mutable.Stack

/**
 * Default implementation of [[org.fusesource.scalate.RenderContext]]
 */
class DefaultRenderContext(val engine: TemplateEngine, var out: PrintWriter) extends RenderContext with Logging {

  val attributes: AttributeMap[String,Any] = new AttributesHashMap[String, Any]() {
    update("context", DefaultRenderContext.this)
  }
  escapeMarkup = engine.escapeMarkup


  /////////////////////////////////////////////////////////////////////
  //
  // RenderContext implementation
  //
  //////////////////////////////////x///////////////////////////////////

  def <<(v: Any): Unit = {
    out.print(value(v, false))
  }

  def <<<(v: Any): Unit = {
    out.print(value(v))
  }


  private val outStack = new Stack[PrintWriter]

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def capture(body: => Unit): String = {
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      val u: Unit = body
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }

  /**
   * Evaluates the template capturing any output written to this page context during the body evaluation
   */
  def capture(template: Template): String = {
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      debug("rendering template " + template)
      template.render(this)
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }

}
