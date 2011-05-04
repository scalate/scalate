/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate

import scala.collection.Set
import collection.mutable.{ListMap, ListBuffer, LinkedHashSet}

/**
 * Represents a small map like thing which is easy to implement on top of any attribute storage mechanism without
 * having to implement a full Map interface. Note that these methods should use the same arguments and return types
 * as their corresponding methods in the mutable Map
 *
 * @version $Revision : 1.1 $
 */

trait AttributeMap {
  
  /**
   * Retries an optional entry for the given attribute
   */
  def get(key: String): Option[Any]

  /**
   * Retrieves the value of the given attribute.
   * 
   * @return the attribute or <code>null</code> in the case where there
   *          is no attribute set using the specified key. 
   */
  def apply(key: String): Any

  /**
   * Updates the value of the given attribute
   */
  def update(key: String, value: Any): Unit

  /**
   * Removes an attribute
   */
  def remove(key: String): Option[Any]

  /**
   * Collects all the available keys
   */
  def keySet: Set[String]

  /**
   * Gets or creates the named linked hash set
   */
  def set[T](name: String): LinkedHashSet[T] = {
    getOrUpdate(name, LinkedHashSet[T]())
  }

  /**
   * Gets or creates the named list buffer
   */
  def list[T](name: String): ListBuffer[T] = {
    getOrUpdate(name, ListBuffer[T]())
  }

  /**
   * Gets or creates the named map
   */
  def map[K,V](name: String): ListMap[K, V] = {
    getOrUpdate(name, ListMap[K, V]())
  }

  /**
   * Gets or creates the named attribute
   */
  def getOrUpdate[T](name: String, value: =>T):T = {
    (get(name) match {
      case Some(rc)=>
        rc
      case None=>
        val rc = value
        update(name, rc)
        rc
    }).asInstanceOf[T]
  }
}
