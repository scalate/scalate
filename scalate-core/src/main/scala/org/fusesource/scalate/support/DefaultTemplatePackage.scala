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

import util.{ Log, ClassLoaders, Files }

object DefaultTemplatePackage extends Log
/**
 * A TemplatePackage where we try and find an object/controller/resource type based on the name of the current template and if we can
 * find it then we create a variable called **it** of the controller type and import its values into the template.
 *
 * This approach can be used for JAXRS controllers or for template views of objects. It avoids having to explicitly
 * import the controller or 'it' variable from the attribute scope
 */
class DefaultTemplatePackage extends TemplatePackage {
  import DefaultTemplatePackage._

  def header(source: TemplateSource, bindings: List[Binding]) = {
    bindings.find(_.name == "it") match {
      case Some(b) =>
        // already has a binding so don't do anything
        ""
      case _ =>
        val cleanUri = source.uri.stripPrefix("/").stripPrefix("WEB-INF/")
        val extensions = cleanUri.split('.').tail
        var className = cleanUri.replace('/', '.')
        extensions.map {
          e =>
            className = Files.dropExtension(className)
            ClassLoaders.findClass(className)
        }.find(_.isDefined).getOrElse(None) match {
          case Some(clazz) =>
            val it = "val " + variableName + " = attribute[_root_." + clazz.getName + "](\"" + variableName + "\")\n"
            if (importMethod) {
              it + "import it._\n"
            } else {
              it
            }

          case _ =>
            if (!className.split('.').last.startsWith("_")) {
              // lets ignore partial templates which are never bound to a resource directly
              debug("Could not find a class on the classpath based on the current url: %s", cleanUri)
            }
            ""
        }
    }
  }

  /**
   * The name of the variable
   */
  def variableName = "it"

  /**
   * Returns whether or not the methods on the variable should be imported
   */
  def importMethod = true
}
