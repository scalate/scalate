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
package org.fusesource.scalate.console

/**
 * Parameters to create a JAXRS resource template
 *
 * @version $Revision: 1.1 $
 */
object ResourceParams {
  def apply(controller: ArchetypeResource): ResourceParams = {
    import controller._
    new ResourceParams(formParam("packageName"), formParam("className"), formParam("resourceUri"))
  }
}
case class ResourceParams(val packageName: String, val className: String, val resourceUri: String) {

  /**
   * Returns the fully qualified class name of the resource to be generated
   */
  def qualifiedClassName = packageName + "." + className
}