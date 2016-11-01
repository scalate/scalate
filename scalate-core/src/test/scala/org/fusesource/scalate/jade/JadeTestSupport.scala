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
package jade

import java.io.{ StringWriter, PrintWriter, File }

import org.fusesource.scalate.scaml.ScamlTestSupport

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class JadeTestSupport extends ScamlTestSupport {

  override def render(name: String, content: String): String = {
    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val uri = "/org/fusesource/scalate/jade/test" + name
    val context = new DefaultRenderContext(uri, engine, out) {
      val name = "Hiram"
      val title = "MyPage"
      val href = "http://scalate.fusesource.org"
      val quality = "scrumptious"
    }

    engine.bindings = List(Binding("context", context.getClass.getName, true))

    val testIdx = testCounter.incrementAndGet
    val dir = new File("target/JadeTest")
    dir.mkdirs
    engine.workingDirectory = dir
    context.attributes("context") = context
    context.attributes("bean") = Bean("red", 10)
    context.attributes("label") = "Scalate"

    val template = compileJade(uri, content)
    template.render(context)
    out.close
    buffer.toString
  }

}