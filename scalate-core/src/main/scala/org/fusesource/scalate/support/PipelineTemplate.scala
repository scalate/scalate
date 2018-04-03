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
package support

import filter.Filter

/**
 * Implements a Template using a list of filters
 */
class PipelineTemplate(
  pipeline: List[Filter],
  text: String) extends Template {

  def render(context: RenderContext) = {
    var rc = text
    for (f <- pipeline) {
      rc = f.filter(context, rc)
    }
    context << rc
  }

  override def toString = "PipelineTemplate(pipeline=" + pipeline + ")"

}
