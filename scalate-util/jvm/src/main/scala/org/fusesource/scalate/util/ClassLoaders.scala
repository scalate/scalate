package org.fusesource.scalate.util

import slogging.LazyLogging

import java.net.URL

object ClassLoaders {

  /**
   * Returns the default class loaders to use for loading which is the current threads context class loader
   * and the class loader which loaded scalate-core by default
   */
  def defaultClassLoaders: List[ClassLoader] = {
    List(Thread.currentThread.getContextClassLoader, classOf[LazyLogging].getClassLoader)
  }

  /**
   * Tries to load the named class on the given class loaders
   */
  def findClass(className: String, classLoaders: Iterable[ClassLoader] = defaultClassLoaders): Option[Class[_]] = {
    def tryLoadClass(classLoader: ClassLoader) = {
      try {
        Some(classLoader.loadClass(className))
      } catch {
        case e: Exception => None
      }
    }
    classLoaders.map(tryLoadClass).find(_.isDefined) match {
      case Some(a) => a
      case _ => None
    }
  }

  /**
   * Tries to find the named resource on the given class loaders
   */
  def findResource(name: String, classLoaders: Iterable[ClassLoader] = defaultClassLoaders): Option[URL] = {
    def tryLoadClass(classLoader: ClassLoader) = {
      try {
        classLoader.getResource(name)
      } catch {
        case e: Exception => null
      }
    }
    classLoaders.map(tryLoadClass).find(_ != null)
  }

  /**
   * Loads the given named class on the given class loaders or fails with a ClassNotFoundException
   */
  def loadClass(className: String, classLoaders: Iterable[ClassLoader]) = findClass(className, classLoaders) match {
    case Some(c) => c
    case _ => throw new ClassNotFoundException(className + " not found in class loaders: " + classLoaders)
  }

  /**
   * Evaluates the given block using the context class loader; then restores the context class loader to its
   * previous value
   */
  def withContextClassLoader[T](classLoader: ClassLoader)(block: => T): T = {
    def thread = Thread.currentThread

    val old = thread.getContextClassLoader
    try {
      thread.setContextClassLoader(classLoader)
      block
    } finally {
      thread.setContextClassLoader(old)
    }

  }
}
