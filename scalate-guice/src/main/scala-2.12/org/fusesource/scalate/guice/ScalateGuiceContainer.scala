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
package org.fusesource.scalate.guice

import _root_.com.google.inject.Injector
import _root_.com.sun.jersey.api.core.ResourceConfig
import _root_.com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import _root_.com.sun.jersey.spi.container.servlet.WebConfig
import _root_.com.sun.jersey.spi.container.WebApplication
import _root_.java.util.{ Map => JMap }

object ScalateGuiceContainer extends Log
/**
 *
 * @version $Revision: 1.1 $
 */
class ScalateGuiceContainer(
  injector: Injector) extends GuiceContainer(injector) {
  import ScalateGuiceContainer._

  // TODO should the GuiceContainer class do this too?
  override def getDefaultResourceConfig(
    props: JMap[String, AnyRef],
    wc: WebConfig): ResourceConfig = injector.getInstance(classOf[ResourceConfig])

  override def initiate(rc: ResourceConfig, wa: WebApplication) = {
    logger.debug("container created with " + rc + " properties: " + rc.getProperties)
    super.initiate(rc, wa)
  }
}
