package org.fusesource.scalate.util

import collection.mutable.HashSet

/**
 * A collection of helper methods
 *
 * @version $Revision: 1.1 $
 */
object Sequences {

  /**
   * Creates a new sequence of the given type without any duplicates
   */
  def removeDuplicates[T](seq: Seq[T]) : Seq[T] = {
    val set = new HashSet[T]()
    seq.filter(e => set.add(e))
  }
}