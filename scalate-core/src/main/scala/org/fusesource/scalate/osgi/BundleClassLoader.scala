package org.fusesource.scalate.osgi

import org.osgi.framework.Bundle

/**
 * A helper class to determine if the class loader provides access to an OSGi Bundle instance
 */
object BundleClassLoader {

  type BundleClassLoader = {
    def getBundle: Bundle
  }

  def unapply(ref: AnyRef): Option[BundleClassLoader] = {
    if (ref == null) return None
    try {
      val method = ref.getClass.getMethod("getBundle")
      if (method.getReturnType == classOf[Bundle])
        Some(ref.asInstanceOf[BundleClassLoader])
      else
        None
    } catch {
      case e: NoSuchMethodException => None
    }
  }
}
