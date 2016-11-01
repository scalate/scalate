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
package org.fusesource.scalate.support

import org.fusesource.scalate.AttributeMap
import scala.collection.JavaConverters._
import java.{ util => ju }

/**
 * The default implementation for <code>AttributeMap</code> backed
 * by a map like collection.
 */
class AttributesHashMap extends AttributeMap {

  private[this] val map = new ju.HashMap[String, Any]

  def get(key: String): Option[Any] = {
    val value = map.get(key)
    if (value == null) None else Some(value)
  }

  def apply(key: String): Any = {
    val answer = map.get(key)
    if (answer == null) {
      throw new NoSuchElementException("key " + key + " not available")
    } else {
      answer
    }
  }

  def update(key: String, value: Any) {
    map.put(key, value)
  }

  def remove(key: String): Option[Any] = {
    val value = map.remove(key)
    if (value == null) None else Some(value)
  }

  def keySet = map.keySet.asScala

  override def toString = map.toString

}