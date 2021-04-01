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
package org.fusesource.scalate.support

import org.fusesource.scalate.util.Strings.isEmpty
import org.fusesource.scalate.util.ClassLoaders
import org.fusesource.scalate.{ Binding, TemplateSource }
import slogging.StrictLogging

/**
 * The base class for any **ScalatePackage** class added to the classpath to customize the templates
 */
abstract class TemplatePackage {

  /**
   * Returns the header to add to the top of the method in the generated Scala code for the typesafe template
   * implementation for languages like ssp, scaml or jade
   */
  def header(source: TemplateSource, bindings: List[Binding]): String
}

object TemplatePackage extends StrictLogging {

  val scalatePackageClassName = "ScalatePackage"

  /**
   * Finds the ScalatePackage class by walking from the templates package up the tree until it finds one
   */
  def findTemplatePackage(source: TemplateSource): Option[TemplatePackage] = {
    def packageWalk(packageName: String): Option[TemplatePackage] = {
      val className = if (isEmpty(packageName))
        scalatePackageClassName
      else
        packageName + "." + scalatePackageClassName

      logger.debug("Trying to find Scalate Package class: " + className)

      ClassLoaders.findClass(className) match {
        case Some(clazz) =>
          logger.debug("using Scalate Package class: " + clazz.getName)
          Some(clazz.getConstructor().newInstance().asInstanceOf[TemplatePackage])

        case _ =>
          if (isEmpty(packageName)) {
            logger.debug("No ScalatePackage class found from templates package: " + source.packageName +
              " on the class loaders: " + ClassLoaders.defaultClassLoaders)
            None
          } else {
            // lets iterate up the package tree looking for a class
            val idx = packageName.lastIndexOf('.')
            val parentPackage = if (idx >= 0) packageName.substring(0, idx) else ""
            packageWalk(parentPackage)
          }
      }
    }

    packageWalk(source.packageName)
  }
}
