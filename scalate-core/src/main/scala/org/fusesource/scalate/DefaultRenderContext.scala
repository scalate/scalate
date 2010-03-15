/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate

import org.fusesource.scalate.util.{Lazy, RenderHelper}
import java.text.{DateFormat, NumberFormat}
import java.util.Locale
import java.io._
import collection.mutable.Stack

/**
 * The RenderContext provides helper methods for interacting with the request, response,
 * attributes and parameters.
 */
class DefaultRenderContext(val engine: TemplateEngine, var out: PrintWriter) extends RenderContext {
  
  val attributes: AttributeMap[String,Any] = new HashMapAttributes[String, Any] {
    override def apply(key: String) =
      if (key == "context") DefaultRenderContext.this else super.apply(key) 
  }


  /////////////////////////////////////////////////////////////////////
  //
  // RenderContext implementation
  //
  //////////////////////////////////x///////////////////////////////////

  def <<(v: Any): Unit = {
    out.print(value(v))
  }

  def <<<(v: Any): Unit = {
    out.print(RenderHelper.sanitize(value(v)))
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
    val buffer = new StringWriter();
    outStack.push(out)
    out = new PrintWriter(buffer)
    try {
      template.render(this)
      out.close()
      buffer.toString
    } finally {
      out = outStack.pop
    }
  }

}
