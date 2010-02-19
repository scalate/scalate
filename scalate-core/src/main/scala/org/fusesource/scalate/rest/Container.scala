package org.fusesource.scalate.rest

/**
 * @version $Revision: 1.1 $
 */
trait Container[K,E] {

  def get(key : K) : Option[E]

  def put(element: E) : Unit

  def key(element: E) : K

  def remove(element: E) : Unit = removeKey(key(element))

  def removeKey(key: K) : Unit
}