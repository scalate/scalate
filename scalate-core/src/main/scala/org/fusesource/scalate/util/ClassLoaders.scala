package org.fusesource.scalate.util

import java.net.URLClassLoader

/**
 * @version $Revision: 1.1 $
 */

object ClassLoaders {

  def classLoaderList[T](aClass: Class[T]): List[String] = {
    classLoaderList(aClass.getClassLoader)
  }

  def classLoaderList[T](classLoader: ClassLoader): List[String] = {
    classLoader match {
      case u: URLClassLoader =>
        u.getURLs.toList.map {_.getFile}

      case _ => Nil
    }
  }


}