package org.fusesource.scalate.support

import _root_.org.fusesource.scalate.AttributeMap

import scala.collection.JavaConversions._
import scala.collection.Set
//import scala.collection.mutable.HashMap

/**
 * The default implementation for <code>AttributeMap</code> backed
 * by a map like collection.
 */
//class AttributesHashMap[A, B] extends HashMap[A,B] with AttributeMap[A, B] {
class AttributesHashMap[A, B] extends AttributeMap[A, B] {

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