package org.fusesource.scalate.jersey

import collection.immutable.HashMap

/**
 * @version $Revision: 1.1 $
 */

trait MapContainer[K,E] extends Container[K,E] {
  var map: Map[K,E] = new HashMap[K,E]()

  def put(element: E) = map = map + (key(element) -> element)

  def get(key: K) : Option[E] = map.get(key)

  def put(elements: E*) : Unit = {
    for (e <- elements) {
      put(e)
    }
  }

  def removeKey(key: K) = map = map - key
}