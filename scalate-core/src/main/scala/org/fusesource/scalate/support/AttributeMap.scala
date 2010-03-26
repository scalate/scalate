package org.fusesource.scalate.support

import scala.collection.JavaConversions._
import scala.collection.Set

/**
 * Represents a small map like thing which is easy to implement on top of any attribute storage mechanism without
 * having to implement a full Map interface. Note that these methods should use the same arguments and return types
 * as their corresponding methods in the mutable Map
 *
 * @version $Revision : 1.1 $
 */

trait AttributeMap[A, B] {
  
  /**
   * Retries an optional entry for the given attribute
   */
  def get(key: A): Option[B]

  /**
   * Retrieves the value of the given attribute.
   * 
   * @return the attribute or <code>null</code> in the case where there
   *          is no attribute set using the specified key. 
   */
  def apply(key: A): B

  /**
   * Updates the value of the given attribute
   */
  def update(key: A, value: B): Unit

  /**
   * Removes an attribute
   */
  def remove(key: A): Option[B]

  /**
   * Collects all the available keys
   */
  def keySet: Set[A]
}

import scala.collection.mutable.HashMap

/**
 * The default implementation for <code>AttributeMap</code> backed
 * by a map like collection.
 */
//class HashMapAttributes[A, B] extends HashMap[A,B] with AttributeMap[A, B] {
class HashMapAttributes[A, B] extends AttributeMap[A, B] {

  import java.util.HashMap

  private[this] val map = new HashMap[A, B]
  
  def get(key: A): Option[B] = {
    val value = map.get(key)
    if (value == null) None else Some(value)
  }
  
  def apply(key: A): B = {
    val answer = map.get(key)
    if (answer == null) {
      throw new NoSuchElementException("key " + key + " not available")
    }
    else {
      answer
    }
  }
  
  def update(key: A, value: B) {
    map.put(key, value)
  }

  def remove(key: A): Option[B] = {
    val value = map.remove(key)
   if (value == null) None else Some(value)
  }

  def keySet = asSet(map.keySet)

  override def toString = map.toString
}