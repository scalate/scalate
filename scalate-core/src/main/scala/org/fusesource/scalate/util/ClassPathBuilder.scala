package org.fusesource.scalate.util

import java.io.File
import java.net.{URI, URLClassLoader}
import scala.collection.mutable.ArrayBuffer


class ClassPathBuilder {
  import ClassPathBuilder._

  private val classpath = new ArrayBuffer[String]
  
  def classPath = Sequences.removeDuplicates(classpath).mkString(File.pathSeparator)
  
  def addClassesDir(dir: String): ClassPathBuilder = addEntry(dir)
  
  def addJar(jar: String): ClassPathBuilder = addEntry(jar)
  
  def addEntry(path: String): ClassPathBuilder = {
    if (path != null && !path.isEmpty)
      classpath += path
    this
  }
  
  def addLibDir(dir: String): ClassPathBuilder = {
    
    def listJars(root: File): Seq[String] = {
      def makeSeq(a: Array[File]): Seq[File] = if (a == null) Nil else a
      if (root.isFile) List(root.toString)
      else makeSeq(root.listFiles) flatMap { f => listJars(f) }
    }
    
    if(dir != null)
      classpath ++= listJars(new File(dir))
    this
  }
  
  def addPathFrom(clazz: Class[_]): ClassPathBuilder = {
    if (clazz != null)
      addPathFrom(clazz.getClassLoader)
    this
  }
  
  def addPathFrom(loader: ClassLoader): ClassPathBuilder = {
    classpath ++= getClassPathFrom(loader)
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
  
  def addJavaPath(): ClassPathBuilder = {
    classpath ++= javaClassPath
    this
  }
}

private object ClassPathBuilder extends Logging {
  
  type AntLikeClassLoader = {
    def getClasspath: String
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
        case e: NoSuchMethodException => None
      }
    }
  }
  
  def getClassPathFrom(classLoader: ClassLoader): Seq[String] = classLoader match {
    
    case null => Nil
    
    case cl: URLClassLoader =>
      for (url <- cl.getURLs.toList) yield {
        // on windows the path can include %20
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4466485
        // so lets use URI as a workaround
        val uri = new URI(url.toString)
        new File(uri.getPath).getCanonicalPath
        //val n = new File(uri.getPath).getCanonicalPath
        //if (n.contains(' ')) {"\"" + n + "\""} else {n}
      }

    case AntLikeClassLoader(acp) =>
      val cp = acp.getClasspath
      cp.split(File.pathSeparator)

    case _ =>
      warn("Cannot introspect on class loader: "+classLoader+" of type "+classLoader.getClass.getCanonicalName)
      val parent = classLoader.getParent
      if (parent != null && parent != classLoader) getClassPathFrom(parent)
      else Nil
  }
  
  def javaClassPath: Seq[String] = {
    val jcp = System.getProperty("java.class.path", "")
    if (jcp.length > 0) jcp.split(File.pathSeparator)
    else Nil
  }
}