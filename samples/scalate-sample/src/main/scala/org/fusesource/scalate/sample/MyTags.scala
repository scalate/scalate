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
package org.fusesource.scalate.sample

import org.fusesource.scalate.RenderContext
import org.fusesource.scalate.RenderContext.capture

/**
 * @version $Revision : 1.1 $
 */

object MyTags {

  /**
   * Implicit version we import the capture method
   */
  def someLayoutWithImportedCapture(body: => Unit) = {
    val text = capture(body)
    "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * Implicit version using an import
   */
  def someLayoutWithRenderContextVariable(body: => Unit) = {
    val context = RenderContext()
    val text = context.capture(body)
   "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * Explicit version where you interact with the context parameter directly
   */
  def someLayoutWithRenderContextParam(context: RenderContext)(body: => Unit) = {
    val text = context.capture(body)
    context << ("<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>")
  }


  //-------------------------------------------------------------------------
  // TODO the following methods dont work!
  //-------------------------------------------------------------------------

  /**
   * TODO not working yet!
   *
   * This option hides the render context
   */
  def someLayout(body: () => String) = {
    val text = body()
    println("found text: " + text)
    "<h3>Wrapped body</h3><p>" + text + "</p><h3>End of wrapped body</h3>"
  }

  /**
   * TODO Not working yet - we currently only support the () => String version
   */
  def someLayoutNotWorking(body: => String) = {
    val text = body
    println("found text: " + text)
    "<h3>Wrapped body</h3><p>" + body + "</p><h3>End of wrapped body</h3>"
  }
}