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

import org.fusesource.scalate.parsers.ssp.SspParser

import scala.reflect.ClassTag
import scala.util.parsing.input.Positional

/**
 * Describes a variable binding that a Scalate template defines.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
case class Binding(
  name: String,
  className: String = "Any",
  importMembers: Boolean = false,
  defaultValue: Option[String] = None,
  kind: String = "val",
  isImplicit: Boolean = false,
  classNamePositional: Option[Positional] = None,
  defaultValuePositional: Option[Positional] = None) {

  def classNamePositionalOrText: String = classNamePositional match {
    case Some(positional) => positional.toString
    case _ => className
  }

  def defaultValuePositionalOrText: String = defaultValuePositional match {
    case Some(positional) => positional.toString
    case _ => defaultValue.get
  }
}

object Binding {

  def apply(expr: String): Binding = {
    val a = (new SspParser).getAttribute(expr)
    Binding(a.name.value, a.className.value, a.autoImport, a.defaultValue.map(_.value), a.kind.value, false)
  }

  def of[T](
    name: String,
    importMembers: Boolean = false,
    defaultValue: Option[String] = None,
    kind: String = "val",
    isImplicit: Boolean = false)(implicit m: ClassTag[T]) =
    new Binding(name, m.runtimeClass.getName, importMembers, defaultValue, kind, isImplicit)
}
