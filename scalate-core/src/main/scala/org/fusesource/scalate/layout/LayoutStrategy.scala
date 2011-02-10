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
package org.fusesource.scalate.layout

import org.fusesource.scalate.{RenderContext, Template}

/**
 * A strategy for loading and applying layout templates.
 * 
 * @version $Revision : 1.1 $
 */
trait LayoutStrategy {
  
  /**
   * Render the specified template by decorating it with another "layout template".
   * 
   * @param template the template to render
   * @param context  the context that will be used for rendering the template
   */
  def layout(template: Template, context: RenderContext): Unit
}

/**
 * A <code>LayoutStrategy</code> that renders the given template without
 * using any layout. 
 */
object NullLayoutStrategy extends LayoutStrategy {
  def layout(template: Template, context: RenderContext) = template.render(context)
}