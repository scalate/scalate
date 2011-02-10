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
package org.fusesource.scalate.sbt

import _root_.sbt._

import java.io.File
import java.net.{URL, URLClassLoader}
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

/**
 * Base trait for Scalate tool support.
 */
trait ScalateProject extends BasicScalaProject {
  /**
   * The name of the bootstrap class.  If None, the tool will attempt to load
   * a default class.
   */
  def scalateBootClassName: Option[String] = None

  /**
   * The directories to search for Scalate templates.
   */
  def scalateSources: PathFinder = mainResources

  /**
   * Runs a block of code with the Scalate classpath as the context class
   * loader.  The Scalate classpath is the [[runClassPath]] plus the
   * [[buildScalaInstance]]'s jars.
   */
  protected def withScalateClassLoader[A](f: ClassLoader => A): A = {
    val oldLoader = Thread.currentThread.getContextClassLoader
    val sitegenPath = buildScalaInstance.jars.foldLeft(runClasspath) { 
       (cp, jar) => cp +++ Path.fromFile(jar)
    }
    val loader = ClasspathUtilities.toLoader(sitegenPath)
    Thread.currentThread.setContextClassLoader(loader)
    try {
      f(loader)
    } finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }
  }
}

/**
 * Base trait for Scalate tool support in a web project.
 */
trait ScalateWebProject extends ScalateProject with WebScalaPaths {
  override def scalateSources = super.scalateSources +++ webappResources
}
