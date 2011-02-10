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
package org.fusesource.scalate.jade

import org.fusesource.scalate._
import org.fusesource.scalate.scaml._
import org.fusesource.scalate.support._

/**
 * <p>
 * Scala code generator of a more concise version of haml/scaml inspired by jade:
 * http://github.com/visionmedia/jade
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class JadeCodeGenerator extends ScamlCodeGenerator {

  override def generate(engine: TemplateEngine, source: TemplateSource, bindings: List[Binding]): Code = {

    val uri = source.uri
    val jadeSource = source.text
    val statements = (new JadeParser).parse(jadeSource)

    val builder = new SourceBuilder()
    builder.generate(engine, source, bindings, statements)
    Code(source.className, builder.code, Set(uri), builder.positions)
  }

}