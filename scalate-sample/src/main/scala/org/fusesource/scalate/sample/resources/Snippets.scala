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

package org.fusesource.scalate.sample.resources

import java.util.Date

/**
 * @version $Revision: 1.1 $
 */

object Snippets {
  def cheese = { <h1>Hello at {new Date()}</h1> <p>This is some more text</p> }

  def beer = <h3>mmm I like beer</h3>

  def itemLink(id: String, name: String) = <a href={"foo/" + id} title={"Go to " + name}>{name}</a>
}