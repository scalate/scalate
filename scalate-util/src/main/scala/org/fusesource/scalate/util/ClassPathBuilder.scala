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
package org.fusesource.scalate.util

import java.io.File
import java.net.{ URI, URLClassLoader }
import java.util.jar.{ Attributes, JarFile }
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

class ClassPathBuilder {
  import ClassPathBuilder._

  private[this] val classpath = new ArrayBuffer[String]

  def classPath = {
    val cp = classpath.distinct
    // lets transform to the canonical path to remove duplicates
    val all = (cp ++ findManifestEntries(cp)).map { s => val f = new File(s); if (f.exists) f.getCanonicalPath else s }
    all.distinct.mkString(File.pathSeparator)
  }

  protected def findManifestEntries(cp: collection.Seq[String]): collection.Seq[String] = cp.flatMap { p =>
    var answer: Seq[String] = Nil
    val f = new File(p)
    if (f.exists && f.isFile) {
      val parent = f.getParentFile
      try {
        val jar = new JarFile(f)
        val m = jar.getManifest
        if (m != null) {
          val attrs = m.getMainAttributes
          val v = attrs.get(Attributes.Name.CLASS_PATH)
          if (v != null) {
            answer = v.toString.trim.split("\\s+").toIndexedSeq.map { n =>
              // classpath entries are usually relative to the jar
              if (new File(n).exists) n else new File(parent, n).getPath
            }
            debug("Found manifest classpath values %s in ", answer, f)
          }
        }
      } catch {
        case e: Exception => // ignore any errors probably due to non-jar
          debug(e, "Ignoring exception trying to open jar file: %s", f)
      }
    }
    answer
  }

  def addClassesDir(dir: String): ClassPathBuilder = addEntry(dir)

  def addEntry(path: String): ClassPathBuilder = {
    if (path != null && path.nonEmpty)
      classpath += path
    this
  }

  def addJar(jar: String): ClassPathBuilder = addEntry(jar)

  def addLibDir(dir: String): ClassPathBuilder = {

    def listJars(root: File): Seq[String] = {
      def makeSeq(a: Array[File]): Seq[File] = if (a == null) Nil else a.toIndexedSeq
      if (root.isFile) List(root.toString)
      else makeSeq(root.listFiles) flatMap { f => listJars(f) }
    }

    if (dir != null)
      classpath ++= listJars(new File(dir))
    this
  }

  def addPathFrom(clazz: Class[_]): ClassPathBuilder = {
    if (clazz != null)
      addPathFrom(clazz.getClassLoader)
    this
  }

  def addPathFromContextClassLoader(): ClassPathBuilder = {
    addPathFrom(Thread.currentThread.getContextClassLoader)
    this
  }

  def addPathFromSystemClassLoader(): ClassPathBuilder = {
    addPathFrom(ClassLoader.getSystemClassLoader)
    this
  }

  def addPathFrom(loader: ClassLoader): ClassPathBuilder = {
    classpath ++= getClassPathFrom(Option(loader))
    this
  }

  def addJavaPath(): ClassPathBuilder = {
    classpath ++= javaClassPath
    this
  }
}

private object ClassPathBuilder extends Log {

  type AntLikeClassLoader = {
    def getClasspath: String
  }

  @tailrec
  def getClassPathFrom(optionalClassLoader: Option[ClassLoader], result: mutable.Builder[String, Set[String]] = Set.newBuilder[String]): Set[String] = {
    val next = optionalClassLoader.flatMap {
      case x if x == x.getParent => None
      case x => Option(x.getParent)
    }

    optionalClassLoader match {
      case None => result.result()

      case Some(cl: URLClassLoader) =>
        getClassPathFrom(
          next,
          result ++= {
            for (url <- cl.getURLs.toList; uri = new URI(url.toString); path = uri.getPath; if (path != null)) yield {
              // on windows the path can include %20
              // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4466485
              // so lets use URI as a workaround
              new File(path).getCanonicalPath
              //val n = new File(uri.getPath).getCanonicalPath
              //if (n.contains(' ')) {"\"" + n + "\""} else {n}
            }
          })

      case Some(AntLikeClassLoader(acp)) =>
        val cp = acp.getClasspath
        getClassPathFrom(
          next,
          result ++= cp.split(File.pathSeparator).toIndexedSeq)

      case _ =>
        warn("Cannot introspect on class loader: %s of type %s", optionalClassLoader, optionalClassLoader.getClass.getCanonicalName)
        getClassPathFrom(next, result)
    }
  }

  def javaClassPath: Seq[String] = {
    val jcp = System.getProperty("java.class.path", "")
    if (jcp.nonEmpty) jcp.split(File.pathSeparator).toIndexedSeq
    else Nil
  }

  object AntLikeClassLoader {
    def unapply(ref: AnyRef): Option[AntLikeClassLoader] = {
      if (ref == null) return None
      try {
        val method = ref.getClass.getMethod("getClasspath")
        if (method.getReturnType == classOf[String])
          Some(ref.asInstanceOf[AntLikeClassLoader])
        else
          None
      } catch {
        case _: NoSuchMethodException => None
      }
    }
  }
}
