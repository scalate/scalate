package org.fusesource.scalate.util

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * A simple Object Pool for objects that are expensive to create.
 */
class ObjectPool[T](number: Int, newInstance: () => T) {

  private[this] val size = new AtomicInteger(0)
  private[this] val pool = new ArrayBlockingQueue[T](number)

  def fetch(): T = {
    pool.poll() match {
      case null => createOrBlock
      case o => o.asInstanceOf[T]
    }
  }

  def release(o: T) = {
    pool.offer(o)
  }

  def add(o: T) = {
    pool.add(o)
  }

  private def createOrBlock: T = {
    size.get match {
      case e: Int if e == number => block
      case _ => create
    }
  }

  private def create: T = {
    size.incrementAndGet match {
      case e: Int if e > number =>
        size.decrementAndGet; fetch()
      case e: Int => newInstance()
    }
  }

  private def block: T = {
    pool.take()
  }
}
