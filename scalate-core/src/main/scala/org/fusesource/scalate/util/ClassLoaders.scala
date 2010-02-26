package org.fusesource.scalate.util

import java.io.File
import java.net.URLClassLoader

/**
 * @version $Revision : 1.1 $
 */

object ClassLoaders {
  def classLoaderList[T](aClass: Class[T]): List[String] = {
    classLoaderList(aClass.getClassLoader)
  }

  def classLoaderList[T](classLoader: ClassLoader): List[String] = {
    classLoader match {
      case cl: URLClassLoader =>
        cl.getURLs.toList.map {
          u => val n = new File(u.getFile).getCanonicalPath
          if (n.contains(' ')) {"\"" + n + "\""} else {n}
        }
      case _ => Nil
    }
  }


}