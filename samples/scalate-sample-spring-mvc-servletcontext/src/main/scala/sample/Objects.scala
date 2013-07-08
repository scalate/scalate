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
package sample

trait SomeTrait {
  def someValue = "This is someValue from a trait"
  def anotherValue = "This is someValue from a trait"
}

case class SomeClass() extends SomeTrait {
  override def anotherValue = "This is anotherValue from a class"
}

class SomeForm {
  @scala.reflect.BeanProperty var s: String = _
}

case class SomeMessage(message: String)

// This is created before being passed into a spring method, so needs a default constructor.
class SomeMessages() {
  @scala.reflect.BeanProperty var messages: List[SomeMessage] = List()
}