package org.fusesource.scalate.spring.config

import org.springframework.context.annotation._
import org.fusesource.scalate.spring.controller.IndexController
import org.fusesource.scalate.spring.view.ScalateViewResolver

class AppConfig {

  @Bean
  def indexController = new IndexController

  @Bean
  def viewResolver = new ScalateViewResolver
}
