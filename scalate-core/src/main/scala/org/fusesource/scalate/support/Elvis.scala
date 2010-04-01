package org.fusesource.scalate.support

/**
 * Implements the Groovy style Elvis operator in Scala
 * 
 * @version $Revision: 1.1 $
 */
class Elvis(val defaultValue: Any) {
  def ?:(value: Any) = if (value != null) value else defaultValue
}
