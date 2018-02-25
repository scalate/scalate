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

import scala.util.control.NoStackTrace
import scala.util.parsing.input.{ NoPosition, Position }
import support.CompilerError

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class TemplateException(
  message: String,
  cause: Throwable) extends RuntimeException(message, cause) {

  def this(message: String) {
    this(message, null)
  }
}

/**
 * Indicates a syntax error trying to parse the template
 */
class InvalidSyntaxException(
  val brief: String,
  val pos: Position = NoPosition) extends TemplateException(brief + " at " + pos) {

  var source: TemplateSource = _

  def template: String = if (source != null) source.uri else null

}

/**
 * Indicates a Scala compiler error occurred when converting the template into bytecode
 */
class CompilerException(
  msg: String,
  val errors: List[CompilerError]) extends TemplateException(msg)

class NoValueSetException(
  val attribute: String) extends TemplateException("The value for '" + attribute + "' was not set")

class NoFormParameterException(
  val parameter: String) extends TemplateException("The form parameter '" + parameter + "' was not set")

class NoSuchViewException(
  val model: AnyRef,
  val view: String) extends TemplateException("No '" + view +
  "' view template could be found for model object '" + model + "' of type: " + model.getClass.getCanonicalName)

class NoSuchFilterException(
  val filter: String) extends TemplateException("No '" + filter + "' filter available.")

class NoInjectionException(
  val injectClass: Class[_]) extends TemplateException("Could not inject type  '" + injectClass + "' was not set")

class StaleCacheEntryException(
  source: TemplateSource) extends TemplateException("The compiled template for " + source + " needs to get recompiled") with NoStackTrace
