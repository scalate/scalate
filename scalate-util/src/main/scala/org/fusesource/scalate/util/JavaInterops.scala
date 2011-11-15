package org.fusesource.scalate.util

/**
 * Some helper methods for calling from Java
 */
object JavaInterops {

  def toImmutableMap[K,V](map: collection.mutable.Map[K,V]): collection.immutable.Map[K,V] = {
    collection.immutable.Map(map.toList: _*)
  }

  def toMutableMap[K,V](map: collection.immutable.Map[K,V]): collection.mutable.Map[K,V] = {
    collection.mutable.HashMap(map.toList: _*)
  }
}
