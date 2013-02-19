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
package org.fusesource.scalate.sample.springmvc
package config

import controller.IndexController
import org.springframework.context.annotation._
import org.fusesource.scalate.spring.view.ScalateViewResolver
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@ComponentScan(Array("org.fusesource.scalate.sample.springmvc"))
@EnableWebMvc
class AppConfig {

  @Bean
  def indexController = new IndexController
  
  @Bean
  def scalateViewResolver: ScalateViewResolver = {
    val viewResolver = new ScalateViewResolver()
    viewResolver.setPrefix("/WEB-INF/view/")
    viewResolver.setOrder(1)
    viewResolver.setSuffix(".scaml")
    viewResolver;
  }
}
