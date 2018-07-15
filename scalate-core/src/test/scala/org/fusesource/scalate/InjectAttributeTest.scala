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

import java.io.File

import org.scalatest.ConfigMap

import scala.collection.immutable.Map

class InjectAttributeTest extends TemplateTestSupport {

  test("Using render context directly") {
    val helper = context.inject[SomeHelper]
    assert(helper != null)
    log.info("got helper! " + helper)
  }

  // in the following test, the compiler does not pass in the
  // attributes type from the left hand side, which is quite surprising at first
  // I guess type inferencing only goes from right to left; not left to right
  val compilerInfersTypeParamsFromTypeOfLHS = false
  if (compilerInfersTypeParamsFromTypeOfLHS) {
    test("Using render context directly without explicit type param") {
      val helper: SomeHelper = context.inject
      assert(helper != null)
      log.info("got helper! " + helper)
    }
  }

  test("template using injection") {
    assertUriOutputContains("/org/fusesource/scalate/ioc.ssp", Map("name" -> "James"), "Hello James!")
  }

  def context = new DefaultRenderContext("dummy.ssp", engine)

  override protected def beforeAll(configMap: ConfigMap) = {
    super.beforeAll(configMap)
    engine.sourceDirectories = List(new File(baseDir, "src/test/resources"))
  }
}

class SomeHelper(context: RenderContext) {
  def greeting = "Hello " + context.attributeOrElse("name", "Unknown") + "!"
}
