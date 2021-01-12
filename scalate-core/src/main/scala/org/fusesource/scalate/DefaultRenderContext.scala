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
package org.fusesource.scalate

import java.io._
import org.fusesource.scalate.resource.{ Resource, StreamResource }
import org.fusesource.scalate.resource.Resource
import org.fusesource.scalate.support.AttributesHashMap
import org.fusesource.scalate.util.IOUtil._
import slogging.StrictLogging

import scala.collection.mutable.Stack

/**
 * Default implementation of [[org.fusesource.scalate.RenderContext]]
 */
class DefaultRenderContext(
  private[this] val _requestUri: String,
  val engine: TemplateEngine,
  var out: PrintWriter = new PrintWriter(new StringWriter())) extends RenderContext with StrictLogging {

  val attributes: AttributeMap = new AttributesHashMap() {
    update("context", DefaultRenderContext.this)
  }
  escapeMarkup = engine.escapeMarkup

  /////////////////////////////////////////////////////////////////////
  //
  // RenderContext implementation
  //
  //////////////////////////////////x///////////////////////////////////

  def requestUri = _requestUri

  def requestResource: Option[Resource] = engine.resourceLoader.resource(requestUri)

  def requestFile: Option[File] = requestResource match {
    case Some(r) => r.asInstanceOf[StreamResource].toFile
    case _ => None
  }

  def <<(v: Any): Unit = {
    out.print(value(v, false).toString)
  }

  def <<<(v: Any): Unit = {
    out.print(value(v).toString)
  }

  private[this] val outStack = new Stack[PrintWriter]

  /**
   * Evaluates the body capturing any output written to this page context during the body evaluation
   */
  def capture(body: => Unit): String = {
    val buffer = new StringWriter()
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      body
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
    val buffer = new StringWriter()
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      logger.debug("rendering template %s", template)
      template.render(this)
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }

  def flush() = out.flush

}
