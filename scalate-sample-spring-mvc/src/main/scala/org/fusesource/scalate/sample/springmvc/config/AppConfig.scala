package org.fusesource.scalate.sample.springmvc
package config

import controller.IndexController
import org.springframework.context.annotation._
import org.fusesource.scalate.spring.view.ScalateViewResolver

class AppConfig {

  @Bean
  def indexController = new IndexController

  @Bean
  def viewResolver = new ScalateViewResolver
}
