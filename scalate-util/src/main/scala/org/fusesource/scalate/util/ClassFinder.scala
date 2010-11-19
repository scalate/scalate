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
package org.fusesource.scalate.util

import java.net.{URLClassLoader, URL}
import java.util.Properties
import java.io.{InputStream, File}

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ClassFinder extends Logging {

  def discoverCommands[T](indexPath:String, classLoaders: List[ClassLoader] = ClassLoaders.defaultClassLoaders): List[T] = {
    classLoaders.flatMap{ cl=>
      ClassLoaders.withContextClassLoader(cl) {
        discoverCommandClasses(indexPath, cl).flatMap {
          name =>
            try {
              val clazz = cl.loadClass(name)
              try {
                Some(clazz.newInstance.asInstanceOf[T])
              } catch {
                case e: Exception =>
                  // It may be a scala object.. check for a module class
                  try {
                    val moduleField = cl.loadClass(name + "$").getDeclaredField("MODULE$")
                    Some(moduleField.get(null).asInstanceOf[T])
                  } catch {
                    case e2: Throwable =>
                      // throw the original error...
                      throw e
                  }
              }
            } catch {
              case e: Throwable =>
                debug("Invalid class: " + name, e)
                None
            }
        }
      }
    }
  }

  def discoverCommandClasses(indexPath:String, cl:ClassLoader=getClass.getClassLoader): List[String] = {
    var rc: List[String] = Nil
    val resources = cl.getResources(indexPath)
    while (resources.hasMoreElements) {
      val url = resources.nextElement;
      debug("loaded commands from " + url)
      val p = loadProperties(url.openStream)
      if (p == null) {
        warn("Could not load class list from: " + url)
      }
      val enum = p.keys
      while (enum.hasMoreElements) {
        rc = rc ::: enum.nextElement.asInstanceOf[String] :: Nil
      }
    }
    rc = rc.distinct
    debug("loaded classes: " + rc)
    return rc
  }

  def loadProperties(is: InputStream): Properties = {
    if (is == null) {
      return null;
    }
    try {
      val p = new Properties()
      p.load(is);
      return p
    } catch {
      case e: Exception =>
        return null
    } finally {
      try {
        is.close()
      } catch {
        case _ =>
      }
    }
  }
  
}