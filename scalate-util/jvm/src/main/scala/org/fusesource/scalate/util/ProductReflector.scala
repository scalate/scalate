package org.fusesource.scalate.util

/**
 * @version $Revision : 1.1 $
 */

object ProductReflector {

  /**
   * A helper method that uses reflection to find the methods taking no arguments to be able to display the fields in a case class
   */
  def toMap(obj: AnyRef) = {
    val c = obj.getClass
    val casemethods = accessorMethods(c)
    val values = casemethods.map(_.invoke(obj))
    casemethods.map(_.getName).zip(values).foldLeft(Map[String, Any]())(_ + _)
  }

  def accessorMethods(c: Class[_]) = {
    val predefined = List("copy$default$1", "copy$default$2", "curry", "curried", "$tag", "productArity", "productElements", "productIterator", "productPrefix", "hashCode", "toString", "tuple", "tupled")

    c.getMethods.toList.filter {
      n =>
        (n.getParameterTypes.size == 0) &&
          (n.getDeclaringClass != classOf[Object]) &&
          !predefined.exists(_ == n.getName) &&
          !n.getName.matches("_\\d+")
    }
  }
}
