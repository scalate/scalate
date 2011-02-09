package org.fusesource.scalate.sbt

import _root_.sbt._

import java.io.File
import java.net.{URL, URLClassLoader}
import java.{util => ju}
import scala.collection.jcl
import scala.collection.jcl.Conversions._

trait ScalateProject extends BasicWebScalaProject with MavenStyleWebScalaPaths {
  def scalateBootClassName: Option[String] = None

  /**
   * Runs a block of code with the given context class loader.
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
