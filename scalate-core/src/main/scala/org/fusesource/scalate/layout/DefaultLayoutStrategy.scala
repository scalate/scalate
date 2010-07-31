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

package org.fusesource.scalate.layout

import org.fusesource.scalate.{ResourceNotFoundException, RenderContext, Template, TemplateEngine}
import org.fusesource.scalate.util.Logging

/**
 * The default implementation of <code>LayoutStrategy</code>.
 *
 * <p>This implementation will first try to load a layout by using
 * the "layout" attribute of the given template.
 *
 * If the attribute is not found then the defaultLayouts passed to the
 * constructor will be searched in order.
 *
 * @version $Revision : 1.1 $
 */
class DefaultLayoutStrategy(val engine: TemplateEngine, val defaultLayouts: String*) extends LayoutStrategy with Logging {
  def layout(template: Template, context: RenderContext) {

    def isLayoutDisabled(layout: String) = layout.trim.isEmpty

    // lets capture the body to be used for the layout
    val body = context.capture(template)

    // lets try find the default layout
    context.attributes.get("layout") match {
      case Some(layout: String) =>
        if (isLayoutDisabled(layout))
          noLayout(body, context)
        else
        if (!tryLayout(layout, body, context)) {
          noLayout(body, context)
        }

      case _ =>
        val layoutName = defaultLayouts.find(tryLayout(_, body, context))
        if (layoutName.isEmpty) {
          noLayout(body, context)
        }
    }
  }

  private def tryLayout(layoutTemplate: String, body: String, context: RenderContext): Boolean = {
    def removeLayout = {
      context.attributes("scalateLayouts") = context.attributeOrElse[List[String]]("scalateLayouts", List()).filterNot(_ == layoutTemplate)
    }

    try {
      debug("Attempting to load layout: " + layoutTemplate)

      context.attributes("scalateLayouts") = layoutTemplate :: context.attributeOrElse[List[String]]("scalateLayouts", List())
      context.attributes("body") = body
      engine.load(layoutTemplate).render(context)

      debug("layout completed of: " + layoutTemplate)
      true
    } catch {
      case e: ResourceNotFoundException =>
        removeLayout
        debug("No layout for: " + layoutTemplate + ". Exception: " + e, e)
        false
      case e: Exception =>
        removeLayout
        error("Unhandled: " + e, e)
        throw e
    }
  }

  /* Returns Option so it can be used in a for comprehension. */
  private def noLayout(body: String, context: RenderContext) = {
    context << body
    None
  }
}
