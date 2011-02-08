package org.fusesource.scalate.sbt

import _root_.sbt._

import java.io.File
import java.net.{URL, URLClassLoader}
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

trait ScalateProject extends BasicWebScalaProject with MavenStyleWebScalaPaths {
  def scalateBootClassName: Option[String] = None

  protected def scalateClassLoader: ClassLoader = {
    val sitegenPath = buildScalaInstance.jars.foldLeft(runClasspath) { 
       (cp, jar) => cp +++ Path.fromFile(jar)
    }
    ClasspathUtilities.toLoader(sitegenPath)
  }

  /**
   * Runs a block of code with the given context class loader.
   */
  protected def withContextClassLoader[A](classLoader: ClassLoader)(f: ClassLoader => A): A = {
    val oldLoader = Thread.currentThread.getContextClassLoader
    Thread.currentThread.setContextClassLoader(classLoader)
    try {
      f(classLoader)
    }
    finally {
      Thread.currentThread.setContextClassLoader(oldLoader)
    }
  }
}
